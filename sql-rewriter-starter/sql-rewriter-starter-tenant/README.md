# SQL Rewriter Starter - Tenant

租户快速启动模块（注解启用）。

## 用法

1. 在 Spring `@Configuration` 上开启租户 starter：

```java

@Configuration
@EnableTenantSqlRewriter
public class TenantAutoConfig {
}
```

2. 提供一个 `TenantIdProvider` Bean：用于运行时解析当前链路租户 ID（不在注解里写死）。

```java

@Component
public class HeaderTenantIdProvider implements TenantIdProvider {
    @Override
    public Object getTenantId() {
        // 从请求头/上下文中解析 tenantId
        return "TENANT_001";
    }
}
```

3. 在方法（或类）上声明租户 ID 获取方式和目标表-字段组合：

```java

@Service
public class OrderService {

    @TenantMapping(
            tenantId = @TenantId(tenantIdProvider = HeaderTenantIdProvider.class),
            tenantTargets = @TenantTargets({
                    @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id"),
                    @TenantTarget(tableNames = {"users"}, columnName = "tenant_code", priority = 5)
            })
    )
    public void listOrders() {
        // 运行时通过 HeaderTenantIdProvider 获取 tenantId，
        // 并对 orders.tenant_id / users.tenant_code 注入租户过滤条件
    }
}
```

如需使用固定租户值（不依赖 Provider），可以：

```java

@TenantMapping(
        tenantId = @TenantId(value = "TENANT_001"),
        tenantTargets = @TenantTargets({
                @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id")
        })
)
public void listOrders() {
}
```

## 说明

本 starter 支持三段式控制：

- `@EnableTenantSqlRewriter`：开启能力（启动级开关）
- `@TenantMapping`：组合注解（包含 `@TenantId` + `@TenantTargets/@TenantTarget`）
- `TenantIdProvider`：Provider 接口（用于运行时解析 tenantId）

说明：`@TenantId/@TenantTargets/@TenantTarget` 不建议直接用于方法/类（已通过注解目标限制，期望你只使用 `@TenantMapping`
作为入口）。

