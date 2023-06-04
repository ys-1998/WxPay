package work.yspan.jadeplaces.yspan.work.service;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import work.yspan.jadeplaces.yspan.work.models.response.BaseResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

public interface WeChatPayService {

    PrepayWithRequestPaymentResponse pay(BigDecimal bigDecimal,String orderNumber,String description,String notifyUrl,String attach,String code);

    Transaction callback(HttpServletRequest request, HttpServletResponse response) throws IOException;

    Transaction findOrderDetails(String wxOrderNo);

    Transaction findOrderDetailsByOrderNumber(String orderNumber);

    BaseResult<?> closeOrder(String canteenOrderNo);
}
