package work.yspan.jadeplaces.yspan.work.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class WxPayConfig {

    /** 商户号 */
    @Value("${cateen.wx.pay.mchid}")
    public  String merchantId ;

    /** 商户API私钥路径 */
    @Value("${cateen.wx.pay.privateKeyPath}")
    public  String privateKeyPath ;

    /** 商户证书序列号 */
    @Value("${cateen.wx.pay.merchantSerialNumber}")
    public  String merchantSerialNumber ;

    /** 商户APIV3密钥 */
    @Value("${cateen.wx.pay.apiV3key}")
    public  String apiV3key ;

    @Bean(name = "wxConfig")
    public Config createConfig(){
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
                        .privateKeyFromPath(privateKeyPath)
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3key)
                        .build();
        return config;
    }


}
