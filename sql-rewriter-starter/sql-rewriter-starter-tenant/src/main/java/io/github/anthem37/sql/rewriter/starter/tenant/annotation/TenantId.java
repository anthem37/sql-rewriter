package io.github.anthem37.sql.rewriter.starter.tenant.annotation;

import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantIdProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明当前链路的租户 ID 获取方式（固定值或 Provider）。
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantId {

    /**
     * 固定租户 ID 值（tenantIdProvider 未指定时生效）。
     */
    String value() default "";

    /**
     * 租户 ID Provider 类型（可动态从上下文解析 tenantId）。
     * 默认使用固定值 {@link #value()}。
     */
    Class<? extends TenantIdProvider> tenantIdProvider() default TenantIdProvider.class;
}

