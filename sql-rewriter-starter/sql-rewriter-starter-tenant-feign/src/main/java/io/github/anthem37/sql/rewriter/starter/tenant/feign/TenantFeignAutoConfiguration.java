package io.github.anthem37.sql.rewriter.starter.tenant.feign;

import feign.RequestInterceptor;
import io.github.anthem37.sql.rewriter.starter.tenant.springmvc.HeaderTenantIdProvider;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantIdProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring Boot 自动配置：给 Spring Cloud 场景下接入租户透传（Spring MVC + OpenFeign）。
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Configuration
@ConditionalOnClass({HttpServletRequest.class, RequestInterceptor.class})
public class TenantFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TenantIdProvider.class)
    public TenantIdProvider tenantIdProvider(@Value("${sql.rewriter.tenant.header:tenantId}") String tenantHeader) {
        return new HeaderTenantIdProvider(tenantHeader);
    }

    @Bean
    @ConditionalOnMissingBean(RequestInterceptor.class)
    public RequestInterceptor tenantFeignRequestInterceptor(@Value("${sql.rewriter.tenant.header:tenantId}") String tenantHeader) {
        return new TenantFeignRequestInterceptor(tenantHeader);
    }
}

