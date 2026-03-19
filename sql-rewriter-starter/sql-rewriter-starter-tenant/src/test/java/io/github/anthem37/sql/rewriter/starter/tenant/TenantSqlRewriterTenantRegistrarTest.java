package io.github.anthem37.sql.rewriter.starter.tenant;

import io.github.anthem37.sql.rewriter.starter.tenant.annotation.EnableTenantSqlRewriter;
import io.github.anthem37.sql.rewriter.starter.tenant.support.TenantSqlRewriterTenantRegistrar;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link TenantSqlRewriterTenantRegistrar} 单元测试。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantSqlRewriterTenantRegistrarTest {

    @Test
    public void registerBeanDefinitions_shouldRegisterExpectedBeans() {
        AnnotationMetadata metadata = AnnotationMetadata.introspect(AppConfig.class);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        TenantSqlRewriterTenantRegistrar registrar = new TenantSqlRewriterTenantRegistrar();
        registrar.registerBeanDefinitions(metadata, beanFactory);

        Assert.assertTrue(((SingletonBeanRegistry) beanFactory).containsSingleton("sqlRewriterTenantSqlInterceptor"));
        Assert.assertTrue(beanFactory.containsBeanDefinition("sqlRewriterTenantConfigurationCustomizer"));
        Assert.assertTrue(beanFactory.containsBeanDefinition("sqlRewriterTenantContextAspect"));
    }

    @Test
    public void registerBeanDefinitions_shouldBeIdempotent() {
        AnnotationMetadata metadata = AnnotationMetadata.introspect(AppConfig.class);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        TenantSqlRewriterTenantRegistrar registrar = new TenantSqlRewriterTenantRegistrar();
        registrar.registerBeanDefinitions(metadata, beanFactory);
        registrar.registerBeanDefinitions(metadata, beanFactory);

        Assert.assertTrue(((SingletonBeanRegistry) beanFactory).containsSingleton("sqlRewriterTenantSqlInterceptor"));
        Assert.assertTrue(beanFactory.containsBeanDefinition("sqlRewriterTenantConfigurationCustomizer"));
        Assert.assertTrue(beanFactory.containsBeanDefinition("sqlRewriterTenantContextAspect"));
    }

    @Configuration
    @EnableTenantSqlRewriter
    static class AppConfig {
    }
}

