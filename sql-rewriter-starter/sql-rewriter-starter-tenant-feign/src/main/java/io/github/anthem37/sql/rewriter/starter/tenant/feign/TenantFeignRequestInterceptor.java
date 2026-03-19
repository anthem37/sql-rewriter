package io.github.anthem37.sql.rewriter.starter.tenant.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 请求拦截器：把当前线程的租户标识透传到下游请求头。
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Slf4j
public class TenantFeignRequestInterceptor implements RequestInterceptor {

    private final String tenantHeader;

    public TenantFeignRequestInterceptor(String tenantHeader) {
        this.tenantHeader = tenantHeader;
    }

    @Override
    public void apply(RequestTemplate template) {
        TenantConfig config = TenantContext.get();
        if (config == null || config.getConfigItems() == null || config.getConfigItems().isEmpty()) {
            return;
        }

        Object tenantId = null;
        for (TenantConfig.ConfigItem item : config.getConfigItems()) {
            if (item == null) {
                continue;
            }
            tenantId = item.getSelectConditionColumnValue();
            if (tenantId != null) {
                break;
            }
        }
        if (tenantId == null) {
            return;
        }

        String tenantIdString = tenantId.toString();
        if (tenantIdString.trim().isEmpty()) {
            return;
        }

        template.header(tenantHeader, tenantIdString);
    }
}

