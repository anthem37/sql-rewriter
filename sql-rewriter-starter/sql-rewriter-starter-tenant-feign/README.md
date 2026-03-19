# SQL Rewriter Starter - Tenant Feign

将 `sql-rewriter-starter-tenant` 的租户能力接入：

- Spring MVC：提供 `TenantIdProvider`，从请求头读取 `tenantId`
- Feign：提供 `feign.RequestInterceptor`，把当前 `TenantContext` 的租户标识透传到下游请求头

