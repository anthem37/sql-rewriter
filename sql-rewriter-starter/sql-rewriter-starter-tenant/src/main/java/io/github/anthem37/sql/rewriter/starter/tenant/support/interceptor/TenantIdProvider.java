package io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor;

/**
 * 运行时租户 ID 解析器。
 *
 * <p>由业务方提供实现，并注册为 Spring Bean。</p>
 *
 * @author anthem37
 * @since 2026/03/19
 */
public interface TenantIdProvider {

    /**
     * 返回当前链路的租户 ID。
     * <p>可以返回 null（此时租户表达式会退化为 IS NULL/NULLValue 语义）。</p>
     */
    Object getTenantId();
}

