package io.bhex.saas.admin.config;

import io.bhex.base.grpc.client.channel.IGrpcClientPool;
import io.bhex.ex.quote.IQuoteGRpcClientAdaptor;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.config
 * @Author: ming.xu
 * @CreateDate: 08/08/2018 8:42 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Configuration
public class LocalConfig {

    @Bean
    @Autowired
    public IQuoteGRpcClientAdaptor iQuoteGRpcClientAdaptor(IGrpcClientPool pool) {
        return () -> pool.borrowChannel(GrpcClientConfig.QUOTE_CHANNEL_NAME);
    }
}
