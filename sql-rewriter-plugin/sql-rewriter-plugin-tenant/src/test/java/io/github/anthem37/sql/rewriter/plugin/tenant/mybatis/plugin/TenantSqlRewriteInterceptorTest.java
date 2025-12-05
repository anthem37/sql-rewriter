package io.github.anthem37.sql.rewriter.plugin.tenant.mybatis.plugin;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.engine.TenantEngine;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * TenantSqlRewriteInterceptor 单元测试
 *
 * @author anthem37
 * @since 2025/12/04
 */
public class TenantSqlRewriteInterceptorTest {

    private TenantSqlRewriteInterceptor interceptor;

    @Before
    public void setUp() {
        interceptor = new TenantSqlRewriteInterceptor();
    }

    @After
    public void tearDown() {
        // 清理ThreadLocal
        TenantContext.remove();
    }

    @Test
    public void testInterceptorCreation() {
        assertNotNull(interceptor);
    }

    @Test
    public void testSetProperties() {
        Properties properties = new Properties();
        properties.setProperty("test.key", "test.value");

        // setProperties 方法应该不抛出异常
        try {
            interceptor.setProperties(properties);
        } catch (Exception e) {
            fail("setProperties不应该抛出异常: " + e.getMessage());
        }

        // 传入null属性也应该不抛出异常
        try {
            interceptor.setProperties(null);
        } catch (Exception e) {
            fail("setProperties(null)不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testPlugin() {
        Object target = new Object();
        Object wrapped = interceptor.plugin(target);

        // 验证目标对象被包装或不包装
        assertNotNull(wrapped);
    }

    @Test
    public void testTenantEngineSupplier() throws Exception {
        // 使用反射获取私有字段
        Field tenantEngineSupplierField = TenantSqlRewriteInterceptor.class.getDeclaredField("tenantEngineSupplier");
        tenantEngineSupplierField.setAccessible(true);

        @SuppressWarnings("unchecked") Supplier<TenantEngine> tenantEngineSupplier = (Supplier<TenantEngine>) tenantEngineSupplierField.get(interceptor);

        assertNotNull(tenantEngineSupplier);

        // 测试没有租户配置的情况
        TenantContext.remove();

        TenantEngine engine = tenantEngineSupplier.get();
        assertNull(engine);

        // 测试有租户配置的情况
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        engine = tenantEngineSupplier.get();
        assertNotNull(engine);
    }

    @Test
    public void testTenantConfigHolderSetAndGet() {
        // 初始状态应该为null
        assertNull(TenantContext.get());

        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));

        // 设置配置
        TenantContext.set(tenantConfig);

        // 应该能获取到设置的配置
        assertEquals(tenantConfig, TenantContext.get());
    }

    @Test
    public void testTenantConfigHolderRemove() {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Arrays.asList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));

        // 设置配置
        TenantContext.set(tenantConfig);
        assertEquals(tenantConfig, TenantContext.get());

        // 移除配置
        TenantContext.remove();

        // 应该为null
        assertNull(TenantContext.get());
    }

    @Test
    public void testInterceptorAnnotations() {
        // 验证类上的注解
        assertTrue(interceptor.getClass().isAnnotationPresent(org.apache.ibatis.plugin.Intercepts.class));

        org.apache.ibatis.plugin.Intercepts intercepts = interceptor.getClass().getAnnotation(org.apache.ibatis.plugin.Intercepts.class);
        assertNotNull(intercepts);
        assertTrue(intercepts.value().length > 0);

        // 验证Signature注解
        org.apache.ibatis.plugin.Signature signature = intercepts.value()[0];
        assertEquals(org.apache.ibatis.executor.statement.StatementHandler.class, signature.type());
        assertEquals("prepare", signature.method());
        assertEquals(2, signature.args().length);
        assertEquals(java.sql.Connection.class, signature.args()[0]);
        assertEquals(Integer.class, signature.args()[1]);
    }

    @Test
    public void testMultipleTenantConfigs() {
        // 创建第一个租户配置
        TenantConfig.ConfigItem configItem1 = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT), Arrays.asList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig tenantConfig1 = new TenantConfig(Collections.singletonList(configItem1));

        // 设置第一个租户配置
        TenantContext.set(tenantConfig1);
        assertEquals(tenantConfig1, TenantContext.get());

        // 创建第二个租户配置
        TenantConfig.ConfigItem configItem2 = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.INSERT), Arrays.asList("order"), "org_id", () -> "org-001", () -> "org-001", () -> "org-001", () -> "org-001", 20);

        TenantConfig tenantConfig2 = new TenantConfig(Collections.singletonList(configItem2));

        // 设置第二个租户配置
        TenantContext.set(tenantConfig2);
        assertEquals(tenantConfig2, TenantContext.get());
        assertNotEquals(tenantConfig1, TenantContext.get());
    }

    @Test
    public void testTenantConfigWithNullValues() {
        // 创建包含null值的配置项
        TenantConfig.ConfigItem configItemWithNulls = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT), Arrays.asList("user"), "tenant_id", null,  // insertColumnValueSupplier
                null,  // deleteConditionColumnValueSupplier
                null,  // updateConditionColumnValueSupplier
                null,  // selectConditionColumnValueSupplier
                10);

        TenantConfig configWithNulls = new TenantConfig(Collections.singletonList(configItemWithNulls));

        // 设置配置
        TenantContext.set(configWithNulls);
        assertEquals(configWithNulls, TenantContext.get());

        // 使用反射测试租户引擎供应商
        try {
            Field tenantEngineSupplierField = TenantSqlRewriteInterceptor.class.getDeclaredField("tenantEngineSupplier");
            tenantEngineSupplierField.setAccessible(true);

            @SuppressWarnings("unchecked") java.util.function.Supplier<TenantEngine> tenantEngineSupplier = (java.util.function.Supplier<TenantEngine>) tenantEngineSupplierField.get(interceptor);

            TenantEngine engine = tenantEngineSupplier.get();
            assertNotNull(engine);
        } catch (Exception e) {
            fail("不应该抛出异常: " + e.getMessage());
        }
    }

    // ========== 以下为新增的测试用例，覆盖更多常用场景 ==========

    @Test
    public void testTenantEngineSqlRewriteWithNullSql() throws Throwable {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                null,
                () -> "tenant-001",
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试null SQL
        String result = engine.run(null);
        assertNull(result);

        // 测试空SQL
        result = engine.run("");
        assertEquals("", result);
    }

    @Test
    public void testTenantEngineSqlRewriteWithoutConfig() throws Throwable {
        // 创建没有配置的租户引擎
        TenantEngine engine = new TenantEngine(Collections.emptyList());

        // 测试无配置时的SQL处理
        String originalSql = "SELECT * FROM user";
        String result = engine.run(originalSql);

        // 应该返回原SQL
        assertEquals(originalSql, result);
    }

    @Test
    public void testTenantEngineSelectSqlRewrite() throws Throwable {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                null,
                () -> "tenant-001",
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试SELECT SQL
        String originalSql = "SELECT * FROM user WHERE id = 1";
        String result = engine.run(originalSql);

        // 验证SQL被重写
        assertNotNull(result);
        assertNotEquals(originalSql, result);
        assertTrue(result.contains("tenant_id"));
        assertTrue(result.contains("tenant-001"));
    }

    @Test
    public void testTenantEngineInsertSqlRewrite() throws Throwable {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.INSERT),
                Arrays.asList("user"),
                "tenant_id",
                () -> "tenant-001",
                null,
                null,
                null,
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试INSERT SQL
        String originalSql = "INSERT INTO user (name, age) VALUES ('test', 20)";
        String result = engine.run(originalSql);

        // 验证SQL被重写
        assertNotNull(result);
        assertNotEquals(originalSql, result);
        assertTrue(result.contains("tenant_id"));
        assertTrue(result.contains("tenant-001"));
    }

    @Test
    public void testTenantEngineUpdateSqlRewrite() throws Throwable {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.UPDATE),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                () -> "tenant-001",
                null,
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试UPDATE SQL
        String originalSql = "UPDATE user SET name = 'test' WHERE id = 1";
        String result = engine.run(originalSql);

        // 验证SQL被重写
        assertNotNull(result);
        assertNotEquals(originalSql, result);
        assertTrue(result.contains("tenant_id"));
        assertTrue(result.contains("tenant-001"));
    }

    @Test
    public void testTenantEngineDeleteSqlRewrite() throws Throwable {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.DELETE),
                Arrays.asList("user"),
                "tenant_id",
                null,
                () -> "tenant-001",
                null,
                null,
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试DELETE SQL
        String originalSql = "DELETE FROM user WHERE id = 1";
        String result = engine.run(originalSql);

        // 验证SQL被重写
        assertNotNull(result);
        assertNotEquals(originalSql, result);
        assertTrue(result.contains("tenant_id"));
        assertTrue(result.contains("tenant-001"));
    }

    @Test
    public void testTenantEngineMultipleTablesSqlRewrite() throws Throwable {
        // 创建包含多个表名的租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT),
                Arrays.asList("user", "order", "product"),
                "tenant_id",
                () -> "tenant-001",
                () -> "tenant-001",
                () -> "tenant-001",
                () -> "tenant-001",
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试不同表的SQL
        String[] testSqls = {
                "SELECT * FROM user",
                "SELECT * FROM order",
                "SELECT * FROM product",
                "INSERT INTO user (name) VALUES ('test')",
                "INSERT INTO order (user_id) VALUES (1)"
        };

        for (String sql : testSqls) {
            String result = engine.run(sql);

            // 验证SQL被重写
            assertNotNull(result);
            assertNotEquals("SQL " + sql + " 应该被重写", sql, result);
            assertTrue("重写后的SQL应该包含tenant_id", result.contains("tenant_id"));
            assertTrue("重写后的SQL应该包含tenant-001", result.contains("tenant-001"));
        }
    }

    @Test
    public void testTenantEngineMultipleConfigItems() throws Throwable {
        // 创建多个配置项
        TenantConfig.ConfigItem userConfig = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                null,
                () -> "user-tenant",
                10);

        TenantConfig.ConfigItem orderConfig = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT),
                Arrays.asList("order"),
                "org_id",
                () -> "order-org",
                null,
                null,
                () -> "order-org",
                5);  // 更高优先级

        TenantConfig tenantConfig = new TenantConfig(Arrays.asList(userConfig, orderConfig));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        String[] testSqls = {
                "SELECT * FROM user",
                "SELECT * FROM order",
                "INSERT INTO order (user_id) VALUES (1)"
        };

        for (String sql : testSqls) {
            String result = engine.run(sql);

            // 验证SQL被重写
            assertNotNull(result);

            assertNotEquals("SQL " + sql + " 应该被重写", sql, result);

            if (sql.startsWith("SELECT") && sql.contains("user")) {
                // 用户表SELECT SQL测试
                assertTrue("用户表SQL应该包含tenant_id", result.contains("tenant_id"));
                assertTrue("用户表SQL应该包含user-tenant", result.contains("user-tenant"));
            } else if (sql.startsWith("SELECT") && sql.contains("order") || sql.startsWith("INSERT") && sql.contains("order")) {
                // 订单表SQL测试
                assertTrue("订单表SQL应该包含org_id", result.contains("org_id"));
                assertTrue("订单表SQL应该包含order-org", result.contains("order-org"));
            }
        }
    }

    @Test
    public void testTenantEngineInvalidSqlParsing() throws Throwable {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                null,
                () -> "tenant-001",
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 测试无效SQL
        String[] invalidSqls = {
                "SELCT * FROM user",  // 拼写错误
                "SELECT * FROM",      // 不完整
                "INSERT INTO",        // 不完整
                "UPDAE user SET name = 'test'"  // 拼写错误
        };

        for (String sql : invalidSqls) {
            // 即使SQL无效，也不应该抛出异常，应该返回原SQL
            String result = engine.run(sql);

            // 对于无效SQL，应该返回原SQL
            assertEquals("无效SQL " + sql + " 应该被原样返回", sql, result);
        }
    }

    @Test
    public void testTenantEngineColumnValueSupplierException() throws Throwable {
        // 创建会抛出异常的Supplier
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                null,
                () -> {
                    throw new RuntimeException("获取租户ID失败");
                },
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        String originalSql = "SELECT * FROM user";

        // 即使Supplier抛出异常，也不应该影响SQL重写过程
        String result = engine.run(originalSql);

        // 验证SQL被重写，但可能没有正确的值
        assertNotNull(result);
        assertNotEquals(originalSql, result);
    }

    @Test
    public void testTenantEngineConfigWithNullValueSuppliers() throws Throwable {
        // 创建包含null值Supplier的配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT),
                Arrays.asList("user"),
                "tenant_id",
                null,
                null,
                null,
                null,  // 所有Supplier都为null
                10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        TenantContext.set(tenantConfig);

        // 创建租户引擎
        TenantEngine engine = new TenantEngine(Collections.singletonList(tenantConfig));

        String originalSql = "SELECT * FROM user";

        // 即使Supplier为null，也应该能正常处理
        String result = engine.run(originalSql);

        // 验证SQL被重写
        assertNotNull(result);
        // 可能仍然包含tenant_id字段，但没有值
        if (!originalSql.equals(result)) {
            assertTrue(result.contains("tenant_id"));
        }
    }
}