package io.github.anthem37.sql.rewriter.starter.tenant.feign;

import feign.RequestTemplate;
import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContext;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContextHolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * {@link TenantFeignRequestInterceptor} 单元测试。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantFeignRequestInterceptorTest {

    @Test
    public void apply_shouldAddHeaderWhenTenantContextExists() {
        TenantFeignRequestInterceptor interceptor = new TenantFeignRequestInterceptor("tenantId");

        TenantConfig tenantConfig = new TenantConfig(Arrays.asList(
                new TenantConfig.ConfigItem(
                        Arrays.asList(SQLTypeEnum.SELECT),
                        Arrays.asList("orders"),
                        "tenant_id",
                        () -> "t-001",
                        () -> "t-001",
                        () -> "t-001",
                        () -> "t-001",
                        1
                )
        ));

        TenantContext.remove();
        try (TenantContextHolder.AutoCloseableHolder ignored = TenantContextHolder.setConfig(tenantConfig)) {
            RequestTemplate template = new RequestTemplate();
            interceptor.apply(template);

            Assert.assertTrue(template.headers().containsKey("tenantId"));
            Assert.assertEquals("t-001", template.headers().get("tenantId").iterator().next());
        }
    }

    @Test
    public void apply_shouldNotAddHeaderWhenNoTenantContext() {
        TenantFeignRequestInterceptor interceptor = new TenantFeignRequestInterceptor("tenantId");

        TenantContext.remove();

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        Assert.assertFalse(template.headers().containsKey("tenantId"));
    }
}

