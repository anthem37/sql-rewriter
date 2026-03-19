# SQL Rewriter BOM

`sql-rewriter-bom` 是一个给外部消费者使用的 Maven BOM，用于把版本号集中到根工程的 `dependencyManagement`
中，避免你在业务工程里为每个子模块单独维护版本。

## 怎么用

在你的工程 `pom.xml` 的 `dependencyManagement` 里引入：

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.anthem37</groupId>
      <artifactId>sql-rewriter-bom</artifactId>
      <version>${project.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

之后在 `dependencies` 里直接声明各模块的 `artifactId` 和 `version`（如果你愿意也可以不写 version，交给 BOM）。

## 与其它模块的关系

- `sql-rewriter-core` / `sql-rewriter-plugin-tenant` / `sql-rewriter-starter-tenant` /
  `sql-rewriter-starter-tenant-feign` 的版本都由 BOM 统一托管
- BOM 仅做版本管理，不包含运行时逻辑

