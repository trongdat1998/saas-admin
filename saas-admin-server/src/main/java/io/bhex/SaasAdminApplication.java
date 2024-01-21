package io.bhex;

import io.bhex.base.grpc.client.channel.IGrpcClientPool;
import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import io.bhex.broker.common.api.client.geetest.DkGeeTestApi;
import io.bhex.broker.common.api.client.geetest.v3.DKGeeTestV3Api;
import io.bhex.broker.common.api.client.recaptcha.GoogleRecaptchaApi;
import io.bhex.broker.common.api.client.recaptcha.GoogleRecaptchaProperties;
import io.bhex.broker.common.entity.GrpcClientProperties;
import io.bhex.broker.common.objectstorage.AwsObjectStorage;
import io.bhex.broker.common.objectstorage.ObjectStorage;
import io.bhex.ex.quote.IQuoteDataGRpcClientAdaptor;
import io.bhex.saas.admin.config.AwsPublicStorageConfig;
import io.bhex.saas.admin.config.GeeTestConfig;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.Resource;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin
 * @Author: ming.xu
 * @CreateDate: 09/08/2018 8:30 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"io.bhex"}, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "io.bhex.bhop.common.config.GrpcConfig"))
public class SaasAdminApplication {

    @Resource
    private Environment environment;

    @Value("${re-captcha-secret-key:XXX}")
    private String reCaptchaSecretKey;

    @Value("${re-captcha-supplier:none}")
    private String reCaptchaSupplier;

    @Bean(name = "idGenerator")
    public SnowflakeGenerator idGenerator() {
        long datacenterId = environment.getProperty("snowflake.datacenterId", Long.class);
        long workerId = environment.getProperty("snowflake.workerId", Long.class);

        return SnowflakeGenerator.newInstance(datacenterId, workerId);
    }

    @Bean("dkGeeTestApi")
    @Qualifier("geeTestConfig")
    public DkGeeTestApi dkGeeTestApi(GeeTestConfig geeTestConfig) {
        return new DkGeeTestApi(geeTestConfig.getGeeTest());
    }

    @Bean("dkGeeTestV3Api")
    @Qualifier("geeTestConfig")
    public DKGeeTestV3Api dkGeeTestV3Api(GeeTestConfig geeTestConfig) {
        return new DKGeeTestV3Api(reCaptchaSupplier, geeTestConfig.getGeeTest());
    }

    @Bean("googleRecaptchaApi")
    public GoogleRecaptchaApi googleRecaptchaApi() {
        GoogleRecaptchaProperties recaptchaProperties = new GoogleRecaptchaProperties();
        recaptchaProperties.setRecaptchaUrl("https://recaptcha.net/recaptcha/api/siteverify");
        recaptchaProperties.setSecurityKey(reCaptchaSecretKey);
        return new GoogleRecaptchaApi(recaptchaProperties);
    }

    @Bean("objectPublicStorage")
    @Autowired
    public ObjectStorage awsPublicObjectStorage(AwsPublicStorageConfig awsPublicStorageConfig) {
        return AwsObjectStorage.buildFromProperties(awsPublicStorageConfig.getAws());
    }

    @Bean
    @Autowired
    public IQuoteDataGRpcClientAdaptor quoteDataGRpcClientAdaptor(IGrpcClientPool pool) {
        return () -> pool.borrowChannel(GrpcClientConfig.QUOTE_DATA_CHANNEL_NAME);
    }

    @Bean
    @ConfigurationProperties(prefix = "grpc-client")
    public GrpcClientProperties grpcClientProperties() {
        return new GrpcClientProperties();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("TaskScheduler-");
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }

    public static void main(String[] args) {
        SpringApplication.run(SaasAdminApplication.class);
    }


}
