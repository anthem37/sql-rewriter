package io.github.anthem37.sql.rewriter.plugin.tenant.util;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.rule.TenantRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TenantUtils 单元测试
 *
 * @author anthem37
 * @since 2025/12/04
 */
public class TenantUtilsTest {

    private TenantConfig tenantConfig;
    private TenantConfig.ConfigItem configItem;

    @Before
    public void setUp() {
        // 创建租户配置项
        configItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT, SQLTypeEnum.UPDATE, SQLTypeEnum.DELETE), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        tenantConfig = new TenantConfig(Collections.singletonList(configItem));
    }

    @After
    public void tearDown() {
        // 清理ThreadLocal
        TenantContext.remove();
    }

    @Test
    public void testConvert2TenantRule() {
        TenantRule tenantRule = TenantRuleConverter.convertToTenantRule(tenantConfig);

        assertNotNull(tenantRule);
        assertNotNull(tenantRule.getTenantRuleItems());
        assertEquals(1, tenantRule.getTenantRuleItems().size());

        TenantRule.TenantRuleItem ruleItem = tenantRule.getTenantRuleItems().get(0);
        assertEquals(configItem.getRewritableSqlTypes(), ruleItem.getRewritableSqlTypes());
        assertEquals(configItem.getTableNames(), ruleItem.getTableNames());
        assertEquals(configItem.getColumnName(), ruleItem.getColumnName());
        assertEquals(configItem.getInsertColumnValue(), ruleItem.getInsertColumnValue());
        assertEquals(configItem.getDeleteConditionColumnValue(), ruleItem.getDeleteConditionColumnValue());
        assertEquals(configItem.getUpdateConditionColumnValue(), ruleItem.getUpdateConditionColumnValue());
        assertEquals(configItem.getSelectConditionColumnValue(), ruleItem.getSelectConditionColumnValue());
        assertEquals(configItem.getPriority(), ruleItem.getPriority());
    }

    @Test
    public void testConvert2TenantRuleWithNull() {
        try {
            TenantRule tenantRule = TenantRuleConverter.convertToTenantRule(null);
            fail("应该抛出 NullPointerException");
        } catch (NullPointerException e) {
            // 预期异常
        }
    }

    @Test
    public void testConvert2TenantItemSqlRules() {
        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(configItem);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 每个表名和每种SQL类型应该对应一个规则
        // 对于每个支持的SQL类型，每个表应该有一个规则
        int expectedRuleCount = configItem.getTableNames().size() * configItem.getRewritableSqlTypes().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithNull() {
        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(null);

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithEmptySqlTypes() {
        TenantConfig.ConfigItem emptySqlTypesConfigItem = new TenantConfig.ConfigItem(Collections.emptyList(), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(emptySqlTypesConfigItem);

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithEmptyTableNames() {
        TenantConfig.ConfigItem emptyTableNamesConfigItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT, SQLTypeEnum.UPDATE, SQLTypeEnum.DELETE), Collections.emptyList(), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(emptyTableNamesConfigItem);

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithSelectOnly() {
        TenantConfig.ConfigItem selectOnlyConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(selectOnlyConfigItem);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 只有一种SQL类型，每个表应该有一个规则
        int expectedRuleCount = selectOnlyConfigItem.getTableNames().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithInsertOnly() {
        TenantConfig.ConfigItem insertOnlyConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.INSERT), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(insertOnlyConfigItem);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 只有一种SQL类型，每个表应该有一个规则
        int expectedRuleCount = insertOnlyConfigItem.getTableNames().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithUpdateOnly() {
        TenantConfig.ConfigItem updateOnlyConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.UPDATE), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(updateOnlyConfigItem);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 只有一种SQL类型，每个表应该有一个规则
        int expectedRuleCount = updateOnlyConfigItem.getTableNames().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithDeleteOnly() {
        TenantConfig.ConfigItem deleteOnlyConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.DELETE), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(deleteOnlyConfigItem);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 只有一种SQL类型，每个表应该有一个规则
        int expectedRuleCount = deleteOnlyConfigItem.getTableNames().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testTenantConfigHolderSetAndGet() {
        // 初始状态应该为null
        assertNull(TenantContext.get());

        // 设置配置
        TenantContext.set(tenantConfig);

        // 应该能获取到设置的配置
        assertEquals(tenantConfig, TenantContext.get());
    }

    @Test
    public void testTenantConfigHolderRemove() {
        // 设置配置
        TenantContext.set(tenantConfig);
        assertEquals(tenantConfig, TenantContext.get());

        // 移除配置
        TenantContext.remove();

        // 应该为null
        assertNull(TenantContext.get());
    }

    @Test
    public void testTenantConfigHolderRemoveWhenNull() {
        // 初始状态为null
        assertNull(TenantContext.get());

        // 移除null配置不应该抛出异常
        TenantContext.remove();

        // 仍然应该为null
        assertNull(TenantContext.get());
    }

    @Test
    public void testTenantConfigHolderSetMultipleTimes() {
        // 设置第一个配置
        TenantContext.set(tenantConfig);
        assertEquals(tenantConfig, TenantContext.get());

        // 创建第二个配置
        TenantConfig.ConfigItem secondConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("product"), "org_id", () -> "org-001", () -> "org-001", () -> "org-001", () -> "org-001", 20);

        TenantConfig secondTenantConfig = new TenantConfig(Collections.singletonList(secondConfigItem));

        // 设置第二个配置
        TenantContext.set(secondTenantConfig);

        // 应该获取到第二个配置
        assertEquals(secondTenantConfig, TenantContext.get());
        assertNotEquals(tenantConfig, TenantContext.get());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithNullValues() {
        TenantConfig.ConfigItem configItemWithNullValues = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT), Arrays.asList("user", "order"), "tenant_id", null,  // insertColumnValueSupplier
                null,  // deleteConditionColumnValueSupplier
                null,  // updateConditionColumnValueSupplier
                null,  // selectConditionColumnValueSupplier
                10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(configItemWithNullValues);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 每个表和每种SQL类型应该对应一个规则
        int expectedRuleCount = configItemWithNullValues.getTableNames().size() * configItemWithNullValues.getRewritableSqlTypes().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithDifferentValueTypes() {
        TenantConfig.ConfigItem configItemWithDifferentValues = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT), Arrays.asList("user", "order"), "tenant_id", () -> 123,  // Integer
                () -> "tenant-001",  // String
                () -> true,  // Boolean
                () -> 456.789,  // Double
                10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(configItemWithDifferentValues);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 每个表和每种SQL类型应该对应一个规则
        int expectedRuleCount = configItemWithDifferentValues.getTableNames().size() * configItemWithDifferentValues.getRewritableSqlTypes().size();
        assertEquals(expectedRuleCount, rules.size());
    }

    @Test
    public void testConvert2TenantItemSqlRulesWithSingleTable() {
        TenantConfig.ConfigItem singleTableConfigItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT), Collections.singletonList("single_table"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        List<ISqlRule<?>> rules = TenantRuleConverter.convertToSqlRules(singleTableConfigItem);

        assertNotNull(rules);
        assertFalse(rules.isEmpty());

        // 只有一个表，但有两种SQL类型，应该有两个规则
        int expectedRuleCount = singleTableConfigItem.getRewritableSqlTypes().size();
        assertEquals(expectedRuleCount, rules.size());
    }
}