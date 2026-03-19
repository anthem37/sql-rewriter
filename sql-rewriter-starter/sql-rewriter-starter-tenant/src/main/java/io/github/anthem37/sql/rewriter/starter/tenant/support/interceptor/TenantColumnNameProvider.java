package io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor;

/**
 * 动态获取租户字段名的 Provider。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public interface TenantColumnNameProvider {
    String getColumnName();
}

