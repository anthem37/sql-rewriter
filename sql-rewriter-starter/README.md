# SQL Rewriter Starter

Starter 模块用于把 `sql-rewriter-core` + 租户能力以“声明式/注解式”方式快速接入 Spring。

## 子模块

- 租户注解接入（MyBatis + Spring AOP）：[
  `sql-rewriter-starter/sql-rewriter-starter-tenant/README.md`](./sql-rewriter-starter-tenant/README.md)
- 租户注解 + Feign 透传（Spring MVC + OpenFeign）：[
  `sql-rewriter-starter/sql-rewriter-starter-tenant-feign/README.md`](./sql-rewriter-starter-tenant-feign/README.md)

## 启用方式（概览）

```mermaid
flowchart TD
  A[@EnableTenantSqlRewriter] --> B[注册：MyBatis 拦截器 + ConfigurationCustomizer + TenantContextAspect]
  C[@TenantMapping] --> D[TenantContextAspect：解析 tenantId + tenantTargets]
  D --> E[TenantContext 写入 TenantConfig]
  E --> F[MyBatis 拦截器：根据 TenantContext 重写 SQL]

  subgraph Feign 扩展
    G[@EnableTenantSqlRewriterFeign] --> H[注册：HeaderTenantIdProvider + TenantFeignRequestInterceptor]
    I[TenantFeignRequestInterceptor] --> J[把 tenantId 写入请求 header]
  end
```

## 下一步

如果你只需要“在当前请求链路内做租户隔离”，优先看 `sql-rewriter-starter-tenant`；
如果还需要把租户透传到下游服务，接着看 `sql-rewriter-starter-tenant-feign`。

