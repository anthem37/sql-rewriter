package io.github.anthem37.sql.rewriter.starter.tenant.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明当前链路需要租户过滤/注入的表-字段组合集合。
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantTargets {
    TenantTarget[] value() default {};
}

