package io.github.anthem37.sql.rewriter.starter.tenant.support;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;

/**
 * 显式向 MyBatis {@link Configuration} 注入租户重写拦截器。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantSqlRewriteConfigurationCustomizer implements ConfigurationCustomizer {

    private final Interceptor interceptor;

    public TenantSqlRewriteConfigurationCustomizer(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void customize(Configuration configuration) {
        configuration.addInterceptor(interceptor);
    }
}

