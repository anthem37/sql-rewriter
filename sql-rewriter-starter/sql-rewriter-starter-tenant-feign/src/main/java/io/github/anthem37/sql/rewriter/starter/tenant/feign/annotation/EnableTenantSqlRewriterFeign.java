package io.github.anthem37.sql.rewriter.starter.tenant.feign.annotation;

import io.github.anthem37.sql.rewriter.starter.tenant.feign.TenantFeignRegistrar;
import io.github.anthem37.sql.rewriter.starter.tenant.support.TenantSqlRewriterAopConfiguration;
import io.github.anthem37.sql.rewriter.starter.tenant.support.TenantSqlRewriterTenantRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 租户 Feign 透传启用注解。
 *
 * <p>该注解会同时启用：</p>
 * <ul>
 *   <li>租户 SQL 重写（`@EnableTenantSqlRewriter`）</li>
 *   <li>Spring MVC 租户标识提供器（从请求头读取）</li>
 *   <li>Feign 请求拦截器（把租户标识透传到下游）</li>
 * </ul>
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        TenantSqlRewriterTenantRegistrar.class,
        TenantSqlRewriterAopConfiguration.class,
        TenantFeignRegistrar.class
})
public @interface EnableTenantSqlRewriterFeign {
}

