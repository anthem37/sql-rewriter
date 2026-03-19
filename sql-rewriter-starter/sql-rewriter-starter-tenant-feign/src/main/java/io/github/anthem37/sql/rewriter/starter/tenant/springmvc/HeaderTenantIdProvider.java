package io.github.anthem37.sql.rewriter.starter.tenant.springmvc;

import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantIdProvider;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Spring MVC 租户 ID 提供器：从当前请求头读取租户标识。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class HeaderTenantIdProvider implements TenantIdProvider {

    private final String tenantHeader;

    public HeaderTenantIdProvider(String tenantHeader) {
        this.tenantHeader = tenantHeader;
    }

    @Override
    public Object getTenantId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }

        HttpServletRequest request = attrs.getRequest();
        if (request == null) {
            return null;
        }

        String tenantId = request.getHeader(tenantHeader);
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return null;
        }
        return tenantId;
    }
}

