package io.github.anthem37.sql.rewriter.starter.tenant.feign;

import feign.RequestInterceptor;
import io.github.anthem37.sql.rewriter.starter.tenant.feign.annotation.EnableTenantSqlRewriterFeign;
import io.github.anthem37.sql.rewriter.starter.tenant.springmvc.HeaderTenantIdProvider;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * {@link TenantFeignRegistrar} 单元测试。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantFeignRegistrarTest {

    @Test
    public void registrar_shouldRegisterFeignBeans() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AppConfig.class);
        context.refresh();

        Assert.assertFalse(context.getBeansOfType(HeaderTenantIdProvider.class).isEmpty());
        Assert.assertFalse(context.getBeansOfType(RequestInterceptor.class).isEmpty());

        // 基础租户 SQL 重写组件：等效于 @EnableTenantSqlRewriter
        Assert.assertNotNull(context.getBean("sqlRewriterTenantSqlInterceptor"));
        Assert.assertNotNull(context.getBean("sqlRewriterTenantConfigurationCustomizer"));
        Assert.assertNotNull(context.getBean("sqlRewriterTenantContextAspect"));

        context.close();
    }

    @Configuration
    @EnableTenantSqlRewriterFeign
    static class AppConfig {
    }
}

