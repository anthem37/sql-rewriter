package io.github.anthem37.sql.rewriter.starter.tenant.springmvc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link HeaderTenantIdProvider} 单元测试。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class HeaderTenantIdProviderTest {

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void getTenantId_shouldReadFromHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("tenantId", "t-001");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        HeaderTenantIdProvider provider = new HeaderTenantIdProvider("tenantId");
        Object tenantId = provider.getTenantId();

        Assert.assertEquals("t-001", tenantId);
    }

    @Test
    public void getTenantId_shouldReturnNullWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        HeaderTenantIdProvider provider = new HeaderTenantIdProvider("tenantId");
        Object tenantId = provider.getTenantId();

        Assert.assertNull(tenantId);
    }
}

