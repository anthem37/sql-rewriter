# SQL Rewriter Starter - Tenant Feign

将 `sql-rewriter-starter-tenant` 的租户能力接入：

- Spring MVC：提供 `TenantIdProvider`，从请求头读取 `tenantId`
- Feign：提供 `feign.RequestInterceptor`，把当前 `TenantContext` 的租户标识透传到下游请求头

## 使用方式

1. 在配置类上启用 Feign 透传：

```java

@Configuration
@EnableTenantSqlRewriterFeign
public class TenantFeignAutoConfig {
}
```

2. 在调用 Feign 的业务方法上声明租户映射，并指定租户 ID 提供器：

```java

@TenantMapping(
        tenantId = @TenantId(tenantIdProvider = HeaderTenantIdProvider.class),
        tenantTargets = @TenantTargets({
                @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id")
        })
)
public void listOrders() {
    // 调用下游 Feign 时会带上 tenantId
}
```

3. 租户 header key 默认是 `tenantId`，也可以通过配置项 `sql.rewriter.tenant.header` 修改。

