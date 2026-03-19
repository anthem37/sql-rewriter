package io.github.anthem37.sql.rewriter.plugin.tenant.engine;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TenantEngine 单元测试
 *
 * @author anthem37
 * @since 2025/12/04
 */
public class TenantEngineTest {

    private TenantEngine tenantEngine;
    private TenantEngine emptyTenantEngine;
    private TenantEngine nullTenantEngine;
    private TenantConfig tenantConfig;
    private TenantConfig emptyTenantConfig;

    @Before
    public void setUp() {
        // 创建正常的租户配置
        TenantConfig.ConfigItem configItem = new TenantConfig.ConfigItem(
                Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT, SQLTypeEnum.UPDATE, SQLTypeEnum.DELETE),
                Arrays.asList("user", "order"),
                "tenant_id",
                () -> "tenant-001",
                () -> "tenant-001",
                () -> "tenant-001",
                () -> "tenant-001",
                10
        );

        tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        tenantEngine = new TenantEngine(Collections.singletonList(tenantConfig));

        // 创建空的租户配置
        emptyTenantConfig = new TenantConfig(Collections.emptyList());
        emptyTenantEngine = new TenantEngine(Collections.singletonList(emptyTenantConfig));

        // 创建null配置的引擎
        nullTenantEngine = new TenantEngine(null);
    }

    @Test
    public void testTenantEngineCreation() {
        assertNotNull(tenantEngine);
        assertNotNull(tenantEngine.getTenantConfigs());
        assertEquals(1, tenantEngine.getTenantConfigs().size());
        assertEquals(tenantConfig, tenantEngine.getTenantConfigs().get(0));
    }

    @Test
    public void testEmptyTenantEngineCreation() {
        assertNotNull(emptyTenantEngine);
        assertNotNull(emptyTenantEngine.getTenantConfigs());
        assertEquals(1, emptyTenantEngine.getTenantConfigs().size());
        assertEquals(emptyTenantConfig, emptyTenantEngine.getTenantConfigs().get(0));
    }

    @Test
    public void testNullTenantEngineCreation() {
        assertNotNull(nullTenantEngine);
        assertNotNull(nullTenantEngine.getTenantConfigs());
        assertTrue(nullTenantEngine.getTenantConfigs().isEmpty());
    }

    @Test
    public void testTenantConfigsImmutability() {
        List<TenantConfig> configs = tenantEngine.getTenantConfigs();

        // 尝试修改返回的列表，应该抛出异常
        try {
            configs.add(new TenantConfig(Collections.emptyList()));
            fail("应该抛出 UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // 预期异常
        }

        try {
            configs.remove(0);
            fail("应该抛出 UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // 预期异常
        }

        try {
            configs.clear();
            fail("应该抛出 UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // 预期异常
        }
    }

    @Test
    public void testMultipleTenantConfigs() {
        // 创建第二个租户配置
        TenantConfig.ConfigItem secondConfigItem = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT),
                Collections.singletonList("product"),
                "org_id",
                () -> "org-001",
                () -> "org-001",
                () -> "org-001",
                () -> "org-001",
                20
        );

        TenantConfig secondTenantConfig = new TenantConfig(Collections.singletonList(secondConfigItem));

        // 创建包含多个租户配置的引擎
        TenantEngine multiConfigEngine = new TenantEngine(Arrays.asList(tenantConfig, secondTenantConfig));

        assertNotNull(multiConfigEngine);
        assertEquals(2, multiConfigEngine.getTenantConfigs().size());
        assertEquals(tenantConfig, multiConfigEngine.getTenantConfigs().get(0));
        assertEquals(secondTenantConfig, multiConfigEngine.getTenantConfigs().get(1));
    }

    @Test
    public void testInheritedMethods() {
        // 测试从父类继承的方法
        assertNotNull(tenantEngine.getRules());

        // 验证规则数量与配置项数量一致
        assertTrue(tenantEngine.getRules().size() > 0);
    }

    @Test
    public void testRunSql() {
        // 测试基本的SQL重写功能
        String selectSql = "SELECT * FROM user";
        String insertSql = "INSERT INTO user (name) VALUES ('test')";
        String updateSql = "UPDATE user SET name = 'updated' WHERE id = 1";
        String deleteSql = "DELETE FROM user WHERE id = 1";

        // 这些调用不应该抛出异常
        String rewrittenSelectSql = tenantEngine.run(selectSql);
        assertNotNull(rewrittenSelectSql);

        String rewrittenInsertSql = tenantEngine.run(insertSql);
        assertNotNull(rewrittenInsertSql);

        String rewrittenUpdateSql = tenantEngine.run(updateSql);
        assertNotNull(rewrittenUpdateSql);

        String rewrittenDeleteSql = tenantEngine.run(deleteSql);
        assertNotNull(rewrittenDeleteSql);
    }

    @Test
    public void testEmptyConfigRunSql() {
        // 测试空配置的SQL重写
        String sql = "SELECT * FROM user";

        // 这些调用不应该抛出异常
        String rewrittenSql = emptyTenantEngine.run(sql);
        assertNotNull(rewrittenSql);
    }

    @Test
    public void testNullConfigRunSql() {
        // 测试null配置的SQL重写
        String sql = "SELECT * FROM user";

        // 这些调用不应该抛出异常
        String rewrittenSql = nullTenantEngine.run(sql);
        assertNotNull(rewrittenSql);
    }

    @Test
    public void testRunWithNonTargetTable() {
        // 测试非目标表的SQL重写
        String sql = "SELECT * FROM non_target_table";

        // 应该不抛出异常，但可能不会修改SQL
        String rewrittenSql = tenantEngine.run(sql);
        assertNotNull(rewrittenSql);
    }

    @Test
    public void testRunWithComplexSql() {
        // 测试复杂SQL的重写
        String complexSql = "SELECT u.*, o.order_id FROM user u JOIN order o ON u.id = o.user_id WHERE u.status = 'active'";

        // 应该不抛出异常
        String rewrittenSql = tenantEngine.run(complexSql);
        assertNotNull(rewrittenSql);
    }

    @Test
    public void testConfigWithNullValues() {
        // 创建包含null值的配置项
        TenantConfig.ConfigItem configItemWithNulls = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT),
                Collections.singletonList("test_table"),
                "tenant_id",
                null,  // insertColumnValueSupplier
                null,  // deleteConditionColumnValueSupplier
                null,  // updateConditionColumnValueSupplier
                () -> "select-tenant-value",  // selectConditionColumnValueSupplier
                10
        );

        TenantConfig configWithNulls = new TenantConfig(Collections.singletonList(configItemWithNulls));
        TenantEngine engineWithNulls = new TenantEngine(Collections.singletonList(configWithNulls));

        assertNotNull(engineWithNulls);
        assertEquals(1, engineWithNulls.getTenantConfigs().size());

        // 应该能够执行SQL重写
        String sql = "SELECT * FROM test_table";
        String rewrittenSql = engineWithNulls.run(sql);
        assertNotNull(rewrittenSql);
    }

    @Test
    public void testEngineWithEmptyTableNames() {
        // 创建包含空表名列表的配置项
        TenantConfig.ConfigItem configItemWithEmptyTables = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT),
                Collections.emptyList(),
                "tenant_id",
                () -> "tenant-001",
                () -> "tenant-001",
                () -> "tenant-001",
                () -> "tenant-001",
                10
        );

        TenantConfig configWithEmptyTables = new TenantConfig(Collections.singletonList(configItemWithEmptyTables));
        TenantEngine engineWithEmptyTables = new TenantEngine(Collections.singletonList(configWithEmptyTables));

        assertNotNull(engineWithEmptyTables);
        assertEquals(1, engineWithEmptyTables.getTenantConfigs().size());

        // 应该能够执行SQL重写
        String sql = "SELECT * FROM any_table";
        String rewrittenSql = engineWithEmptyTables.run(sql);
        assertNotNull(rewrittenSql);
    }

    @Test
    public void testTenantConfigPriorityAffectsWhereConditionOrder() throws Throwable {
        // priority 数值越小优先级越高：期望 "A" 条件先于 "B" 条件出现
        TenantConfig.ConfigItem configB = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT),
                Collections.singletonList("user"),
                "tenant_id",
                null, // insert
                null, // delete
                null, // update
                () -> "B", // select
                2
        );

        TenantConfig.ConfigItem configA = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT),
                Collections.singletonList("user"),
                "tenant_id",
                null, // insert
                null, // delete
                null, // update
                () -> "A", // select
                1
        );

        // 故意把优先级更低的 configB 放前面，验证 TenantRule 会重新按 priority 排序
        TenantConfig config = new TenantConfig(Arrays.asList(configB, configA));
        TenantEngine engine = new TenantEngine(Collections.singletonList(config));

        String result = engine.run("SELECT * FROM user");

        int idxA = result.indexOf("user.tenant_id = 'A'");
        int idxB = result.indexOf("user.tenant_id = 'B'");
        assertTrue("期望 priority 更高（数值更小）的 A 条件先出现", idxA >= 0 && idxB >= 0 && idxA < idxB);
    }
}