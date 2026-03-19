package io.github.anthem37.sql.rewriter.starter.tenant.support;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 为租户 starter 打开 Spring AOP 代理。
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Configuration
@EnableAspectJAutoProxy
public class TenantSqlRewriterAopConfiguration {
}

