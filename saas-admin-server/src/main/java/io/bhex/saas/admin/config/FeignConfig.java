package io.bhex.saas.admin.config;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.bhex.saas.admin.http.*;
import org.springframework.context.annotation.Configuration;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.config
 * @Author: ming.xu
 * @CreateDate: 17/08/2018 11:59 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Configuration
public class FeignConfig {

//    @Resource
//    private Environment environment;
//
//    @Bean
//    @Qualifier("brokerHttpClient")
//    public BrokerHttpClient getExchangeClient() {
//        String url = String.format("http://%s:%d/",
//                environment.getProperty("broker_admin.http.host", String.class),
//                environment.getProperty("broker_admin.http.port",Integer.class));
//        return Feign.builder()
//                .encoder(new JacksonEncoder())
//                .decoder(new JacksonDecoder())
//                .target(BrokerHttpClient.class,url);
//    }

//    @Bean
//    @Qualifier("exchangeGatewayClient")
//    public ExchangeGatewayHttpClient getExchangeGatewayClient() {
//        String url = String.format("http://%s:%d/",
//                environment.getProperty("exchange.gateway.application-name", String.class),
//                environment.getProperty("exchange.gateway.http-port",Integer.class));
//        return Feign.builder()
//                .encoder(new JacksonEncoder())
//                .decoder(new JacksonDecoder())
//                .logger(new Logger.JavaLogger().appendToFile("logs/http.log"))
//                .target(ExchangeGatewayHttpClient.class,url);
//    }
//
//    @Bean
//    @Qualifier("exchangeAdminClient")
//    public ExchangeAdminHttpClient getExchangeAdminClient() {
//        String url = String.format("http://%s:%d/",
//                environment.getProperty("exchange.admin.application-name", String.class),
//                environment.getProperty("exchange.admin.http-port",Integer.class));
//        return Feign.builder()
//                .encoder(new JacksonEncoder())
//                .decoder(new JacksonDecoder())
//                .logger(new Logger.JavaLogger().appendToFile("logs/http.log"))
//                .target(ExchangeAdminHttpClient.class,url);
//    }

//    public static ExchangeAdminHttpClient getExchangeAdminClient(String adminUrl) {
//        return Feign.builder()
//                .encoder(new JacksonEncoder())
//                .decoder(new JacksonDecoder())
//                //.logger(new Logger.JavaLogger().appendToFile("logs/http.log"))
//                .target(ExchangeAdminHttpClient.class, adminUrl);
//    }

    public static ExchangeGatewayHttpClient getExchangeGatewayClient(String gatewayUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                //.logger(new Logger.JavaLogger().appendToFile("logs/http.log"))
                .target(ExchangeGatewayHttpClient.class, gatewayUrl);
    }


    public static BrokerHttpClient getBrokerClient(String adminUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                //.logger(new Logger.JavaLogger().appendToFile("logs/http.log"))
                .target(BrokerHttpClient.class, adminUrl);
    }

    public static BhHttpClient getBhClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .target(BhHttpClient.class, "http://bh-server.bluehelix:7010/");
    }

    public static OtcHttpClient getOtcClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .target(OtcHttpClient.class, "http://otc-server:7241/");
    }


}
