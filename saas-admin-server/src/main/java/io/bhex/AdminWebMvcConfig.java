package io.bhex;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.grpc.client.interceptor.ClientLogInterceptor;
import io.bhex.bhop.common.jwt.filter.AccessAuthorizeInterceptor;
import io.bhex.bhop.common.util.filter.StringXssDeserializer;
import io.bhex.bhop.common.util.filter.XSSRequestFilter;
import io.bhex.broker.common.entity.GrpcChannelInfo;
import io.bhex.broker.common.entity.GrpcClientProperties;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: AdminWebMvcConfig
 * @Date: 2020/1/14 下午3:13
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@EnableWebMvc
@Configuration
public class AdminWebMvcConfig extends WebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Bean
    public AccessAuthorizeInterceptor accessAuthorizeInterceptor() {
        return new AccessAuthorizeInterceptor();
    }

    @Bean
    public FilterRegistrationBean<XSSRequestFilter> xssRequestFilterFilterRegistrationBean() {
        FilterRegistrationBean<XSSRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setName("xssRequestFilter");
        registration.setFilter(new XSSRequestFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessAuthorizeInterceptor()).excludePathPatterns("/internal/**", "/org_api/**", "/error");
    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter methodParameter) {
                return methodParameter.getParameterType().equals(AdminUserReply.class);
            }

            @Override
            public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
                return webRequest.getAttribute("adminUser", 0);
            }
        });
    }

    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                //.enable(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)

                .registerModule(new SimpleModule()
                        // BigDecimal 设置固定的scale 并写成string
                        .addSerializer(BigDecimal.class, new JsonSerializer<BigDecimal>() {
                            @Override
                            public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
                                    throws IOException {
                                gen.writeString(value.stripTrailingZeros().toPlainString());
                            }
                        })
                        // long to string
                        .addSerializer(Long.class, ToStringSerializer.instance)
                        .addSerializer(Long.TYPE, ToStringSerializer.instance));
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converters.add(converter);
        converters.add(new StringHttpMessageConverter());
    }
}
