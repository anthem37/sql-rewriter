package io.github.anthem37.sql.rewriter.starter.tenant.annotation;

import java.lang.annotation.*;

/**
 * 租户映射注解：把 {@link TenantId} 与 {@link TenantTargets} 组合在一个注解里使用。
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantMapping {

    TenantId tenantId();

    TenantTargets tenantTargets();
}

