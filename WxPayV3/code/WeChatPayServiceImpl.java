package work.yspan.jadeplaces.yspan.work.service.impl;

import com.alibaba.fastjson2.JSON;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import work.yspan.jadeplaces.yspan.work.mapper.CateenOrderMapper;
import work.yspan.jadeplaces.yspan.work.models.response.BaseResult;
import work.yspan.jadeplaces.yspan.work.service.WeChatPayService;
import work.yspan.jadeplaces.yspan.work.util.components.SpringUtil;
import work.yspan.jadeplaces.yspan.work.util.components.WxUtil;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;


@Slf4j
@Service
public class WeChatPayServiceImpl implements WeChatPayService {

    @Resource
    private WxUtil wxUtil;

    /**
     * 商户号
     */
    @Value("${cateen.wx.pay.mchid}")
    public String merchantId;

    /**
     * 小程序 appid
     */
    @Value("${cateen.wx.appid}")
    public String appid;


    @Resource
    private CateenOrderMapper cateenOrderMapper;

    @Override
    public PrepayWithRequestPaymentResponse pay(BigDecimal bigDecimal, String orderNumber,String description,String notifyUrl,String attach,String code) {

        //        自动更新平台证书的配置类
        Config wxConfig = (Config) SpringUtil.getBean("wxConfig");

        //设置请求内容
        PrepayRequest request = new PrepayRequest();
        BigDecimal b2 = bigDecimal.multiply(new BigDecimal(100));
        Integer total = new Integer(b2.intValue());
        Amount amount = new Amount();
        amount.setTotal(total);//设置钱分为单位
        request.setAmount(amount);
        request.setAppid(appid);//appid
        request.setMchid(merchantId);//商户号
        request.setDescription(description);
        request.setNotifyUrl(notifyUrl);//回调地址
        request.setOutTradeNo(orderNumber);//商户订单号
        request.setAttach(attach);//附加信息

        String openid = wxUtil.wxLoginOpenid(code);
        log.info("钱：{} 回调地址{} 商户点单号{}",total,notifyUrl,orderNumber);
        //设置支付人
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        //构建请求方法
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(wxConfig).build();
        //获取预支付信息用于前端吊起微信支付
        PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
        log.info("请求微信生成预支付订单{}",response.toString());
        return response;
    }

    @Override
    public Transaction callback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("进入回调方法");
        //获取请求头数据
        String signature = request.getHeader("Wechatpay-Signature");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String Serial = request.getHeader("Wechatpay-Serial");
        String signatureType = request.getHeader("Wechatpay-Signature-Type");
        //获取请求体数据
        BufferedReader reader = request.getReader();
        StringBuilder builder = new StringBuilder();
        String str = null;
        while ((str = reader.readLine()) != null) {
            builder.append(str);
        }

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(Serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(builder.toString())
                .build();

        //获取自动更新的证书
        NotificationConfig wxConfig = (NotificationConfig) SpringUtil.getBean("wxConfig");
        //验签、解密并将 JSON 转换成具体的通知回调对象
        NotificationParser parser = new NotificationParser(wxConfig);
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        log.info("解密微信回调数据成功=>{}", JSON.toJSONString(transaction));

        return transaction;

    }

    @Override
    public Transaction findOrderDetails(String wxOrderNo) {
        //        自动更新平台证书的配置类
        Config wxConfig = (Config) SpringUtil.getBean("wxConfig");

        //构建请求方法
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(wxConfig).build();

        QueryOrderByIdRequest queryRequest = new QueryOrderByIdRequest();
        queryRequest.setMchid(merchantId);
        queryRequest.setTransactionId(wxOrderNo);

        try {
            Transaction result = service.queryOrderById(queryRequest);
            log.info("订单查询结果:{}",result.toString());
            return result;
        } catch (ServiceException e) {
            // API返回失败, 例如ORDER_NOT_EXISTS
            System.out.printf("code=[%s], message=[%s]\n", e.getErrorCode(), e.getErrorMessage());
            System.out.printf("reponse body=[%s]\n", e.getResponseBody());
        }

        return null;
    }

    public Transaction findOrderDetailsByOrderNumber(String orderNumber) {
        //        自动更新平台证书的配置类
        Config wxConfig = (Config) SpringUtil.getBean("wxConfig");

        //构建请求方法
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(wxConfig).build();

        QueryOrderByOutTradeNoRequest queryOrderByOutTradeNoRequest = new QueryOrderByOutTradeNoRequest();

        queryOrderByOutTradeNoRequest.setMchid(merchantId);
        queryOrderByOutTradeNoRequest.setOutTradeNo(orderNumber);
        Transaction transaction = service.queryOrderByOutTradeNo(queryOrderByOutTradeNoRequest);

        return transaction;
    }

    @Override
    public BaseResult<?> closeOrder(String canteenOrderNo) {
        //        自动更新平台证书的配置类
        Config wxConfig = (Config) SpringUtil.getBean("wxConfig");

        //构建请求方法
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(wxConfig).build();

        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid(merchantId);
        closeRequest.setOutTradeNo(canteenOrderNo);
        // 方法没有返回值，意味着成功时API返回204 No Content
        service.closeOrder(closeRequest);
        return null;
    }
}
