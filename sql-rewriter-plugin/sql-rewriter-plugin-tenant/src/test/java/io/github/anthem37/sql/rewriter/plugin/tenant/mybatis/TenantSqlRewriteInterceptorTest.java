package io.github.anthem37.sql.rewriter.plugin.tenant.mybatis;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

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

        @SuppressWarnings("unchecked") java.util.function.Supplier<Object> tenantEngineSupplier = (java.util.function.Supplier<Object>) tenantEngineSupplierField.get(interceptor);

        assertNotNull(tenantEngineSupplier);

        // 测试没有租户配置的情况
        TenantContext.remove();

        Object engine = tenantEngineSupplier.get();
        assertNull(engine);

        // 测试有租户配置的情况
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT), Arrays.asList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

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
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT), Arrays.asList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig tenantConfig = new TenantConfig(Collections.singletonList(configItem));

        // 设置配置
        TenantContext.set(tenantConfig);

        // 应该能获取到设置的配置
        assertEquals(tenantConfig, TenantContext.get());
    }

    @Test
    public void testTenantConfigHolderRemove() {
        // 创建租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT), Arrays.asList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

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

            @SuppressWarnings("unchecked") java.util.function.Supplier<Object> tenantEngineSupplier = (java.util.function.Supplier<Object>) tenantEngineSupplierField.get(interceptor);

            Object engine = tenantEngineSupplier.get();
            assertNotNull(engine);
        } catch (Exception e) {
            fail("不应该抛出异常: " + e.getMessage());
        }
    }
}