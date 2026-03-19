package io.github.anthem37.sql.rewriter.starter.tenant.annotation;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantColumnNameProvider;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantTableNamesProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 单个表-字段租户过滤配置：
 * - tableNames / tableNamesProvider 二选一（固定 or 动态）
 * - columnName / columnNameProvider 二选一（固定 or 动态）
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantTarget {

    /**
     * 固定表名列表（当 tableNamesProvider 未指定时生效）
     */
    String[] tableNames() default {};

    /**
     * 动态获取表名列表的 Provider 类型。
     * 默认值为接口本身，表示使用 {@link #tableNames()}。
     */
    Class<? extends TenantTableNamesProvider> tableNamesProvider() default TenantTableNamesProvider.class;

    /**
     * 固定租户字段名（当 columnNameProvider 未指定时生效）
     */
    String columnName() default "tenant_id";

    /**
     * 动态获取租户字段名的 Provider 类型。
     * 默认值为接口本身，表示使用 {@link #columnName()}。
     */
    Class<? extends TenantColumnNameProvider> columnNameProvider() default TenantColumnNameProvider.class;

    /**
     * 本映射需要改写的 SQL 类型。
     */
    SQLTypeEnum[] sqlTypes() default {
            SQLTypeEnum.SELECT,
            SQLTypeEnum.INSERT,
            SQLTypeEnum.UPDATE,
            SQLTypeEnum.DELETE
    };

    /**
     * 优先级（数值越小越先执行）。
     */
    int priority() default 10;
}

