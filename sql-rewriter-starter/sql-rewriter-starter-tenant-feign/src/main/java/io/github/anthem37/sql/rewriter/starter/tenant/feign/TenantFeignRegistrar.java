package io.github.anthem37.sql.rewriter.starter.tenant.feign;

import io.github.anthem37.sql.rewriter.starter.tenant.springmvc.HeaderTenantIdProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * tenant-feign 注册器：将 Spring MVC 的租户取值与 Feign 的请求透传组件注册到容器中。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantFeignRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String TENANT_HEADER_PLACEHOLDER = "${sql.rewriter.tenant.header:tenantId}";

    private static final String HEADER_PROVIDER_BEAN_NAME = "sqlRewriterHeaderTenantIdProvider";
    private static final String FEIGN_REQUEST_INTERCEPTOR_BEAN_NAME = "sqlRewriterTenantFeignRequestInterceptor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 1) HeaderTenantIdProvider
        String headerProviderClassName = HeaderTenantIdProvider.class.getName();
        if (!containsBeanOfClass(registry, headerProviderClassName)
                && !registry.containsBeanDefinition(HEADER_PROVIDER_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(HeaderTenantIdProvider.class);
            def.getConstructorArgumentValues().addIndexedArgumentValue(0, new TypedStringValue(TENANT_HEADER_PLACEHOLDER));
            registry.registerBeanDefinition(HEADER_PROVIDER_BEAN_NAME, def);
        }

        // 2) TenantFeignRequestInterceptor
        String requestInterceptorClassName = TenantFeignRequestInterceptor.class.getName();
        if (!containsBeanOfClass(registry, requestInterceptorClassName)
                && !registry.containsBeanDefinition(FEIGN_REQUEST_INTERCEPTOR_BEAN_NAME)) {
            RootBeanDefinition def = new RootBeanDefinition(TenantFeignRequestInterceptor.class);
            def.getConstructorArgumentValues().addIndexedArgumentValue(0, new TypedStringValue(TENANT_HEADER_PLACEHOLDER));
            registry.registerBeanDefinition(FEIGN_REQUEST_INTERCEPTOR_BEAN_NAME, def);
        }

        // 3) TenantIdProvider / RequestInterceptor 都会通过类型注入被使用
        //    （这里不做额外注册，避免与用户自定义 Bean 冲突）。
    }

    private boolean containsBeanOfClass(BeanDefinitionRegistry registry, String className) {
        String[] beanNames = registry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String existingClassName = beanDefinition.getBeanClassName();
            if (className.equals(existingClassName)) {
                return true;
            }
        }
        return false;
    }
}

