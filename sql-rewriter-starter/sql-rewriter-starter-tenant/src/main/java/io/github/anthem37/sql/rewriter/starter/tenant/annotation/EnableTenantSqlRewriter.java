package io.github.anthem37.sql.rewriter.starter.tenant.annotation;

import io.github.anthem37.sql.rewriter.starter.tenant.support.TenantSqlRewriterAopConfiguration;
import io.github.anthem37.sql.rewriter.starter.tenant.support.TenantSqlRewriterTenantRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 租户快速启动注解：开启租户 SQL 重写能力（MyBatis 拦截器 + AOP 上下文注入）。
 * <p>
 * 使用方式：
 * <pre>
 * {@code
 * @Configuration
 * @EnableTenantSqlRewriter
 * public class AppConfig {}
 * }
 * </pre>
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({TenantSqlRewriterTenantRegistrar.class, TenantSqlRewriterAopConfiguration.class})
public @interface EnableTenantSqlRewriter {
}

