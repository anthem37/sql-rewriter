package io.github.anthem37.sql.rewriter.starter.tenant.support;

import io.github.anthem37.sql.rewriter.plugin.tenant.mybatis.plugin.TenantSqlRewriteInterceptor;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.EnableTenantSqlRewriter;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantContextAspect;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 读取 {@link EnableTenantSqlRewriter} 注解，并在 Spring 容器中注册：
 * - MyBatis {@link org.apache.ibatis.plugin.Interceptor}（用于执行 SQL 重写）
 * - MyBatis {@link TenantSqlRewriteConfigurationCustomizer}（把拦截器加入 MyBatis 配置）
 * - AOP 切面 {@link TenantContextAspect}（从 {@code @TenantMapping} 解析租户配置）
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantSqlRewriterTenantRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String TENANT_INTERCEPTOR_BEAN_NAME = "sqlRewriterTenantSqlInterceptor";
    private static final String TENANT_CUSTOMIZER_BEAN_NAME = "sqlRewriterTenantConfigurationCustomizer";
    private static final String TENANT_ASPECT_BEAN_NAME = "sqlRewriterTenantContextAspect";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!importingClassMetadata.hasAnnotation(EnableTenantSqlRewriter.class.getName())) {
            return;
        }

        // 1) 注册 MyBatis 拦截器：它会从 TenantContext(ThreadLocal) 取租户配置完成重写
        if (!registry.containsBeanDefinition(TENANT_INTERCEPTOR_BEAN_NAME)) {
            if (registry instanceof SingletonBeanRegistry) {
                SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
                if (!singletonBeanRegistry.containsSingleton(TENANT_INTERCEPTOR_BEAN_NAME)) {
                    singletonBeanRegistry.registerSingleton(TENANT_INTERCEPTOR_BEAN_NAME, new TenantSqlRewriteInterceptor());
                }
            } else {
                RootBeanDefinition interceptorDef = new RootBeanDefinition(TenantSqlRewriteInterceptor.class);
                registry.registerBeanDefinition(TENANT_INTERCEPTOR_BEAN_NAME, interceptorDef);
            }
        }

        // 2) 注册 MyBatis ConfigurationCustomizer（把拦截器加入 MyBatis 配置）
        if (!registry.containsBeanDefinition(TENANT_CUSTOMIZER_BEAN_NAME)) {
            RootBeanDefinition customizerDef = new RootBeanDefinition(TenantSqlRewriteConfigurationCustomizer.class);
            customizerDef.getConstructorArgumentValues().addIndexedArgumentValue(0,
                    new org.springframework.beans.factory.config.RuntimeBeanReference(TENANT_INTERCEPTOR_BEAN_NAME));
            registry.registerBeanDefinition(TENANT_CUSTOMIZER_BEAN_NAME, customizerDef);
        }

        // 3) 注册 AOP 切面：读取 @TenantMapping 并写入 TenantContext
        if (!registry.containsBeanDefinition(TENANT_ASPECT_BEAN_NAME)) {
            RootBeanDefinition aspectDef = new RootBeanDefinition(TenantContextAspect.class);
            registry.registerBeanDefinition(TENANT_ASPECT_BEAN_NAME, aspectDef);
        }
    }
}

