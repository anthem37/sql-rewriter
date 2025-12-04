package io.github.anthem37.sql.rewriter.plugin.tenant.rule;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * TenantRule 单元测试
 *
 * @author anthem37
 * @since 2025/12/04
 */
public class TenantRuleTest {

    private TenantRule tenantRule;
    private TenantConfig tenantConfig;
    private TenantRule.TenantRuleItem tenantRuleItem;
    private TenantConfig.ConfigItem configItem;

    @Before
    public void setUp() {
        // 创建租户配置项
        configItem = new TenantConfig.ConfigItem(Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT, SQLTypeEnum.UPDATE, SQLTypeEnum.DELETE), Arrays.asList("user", "order"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        tenantConfig = new TenantConfig(Collections.singletonList(configItem));
        tenantRule = new TenantRule(tenantConfig);
        tenantRuleItem = tenantRule.getTenantRuleItems().get(0);
    }

    @Test
    public void testTenantRuleCreation() {
        assertNotNull(tenantRule);
        assertNotNull(tenantRule.getTenantRuleItems());
        assertEquals(1, tenantRule.getTenantRuleItems().size());
        assertNotNull(tenantRule.getRules());
        assertTrue(tenantRule.getRules().size() > 0);
    }

    @Test
    public void testTenantRuleItemCreation() {
        assertNotNull(tenantRuleItem);
        assertEquals(configItem.getRewritableSqlTypes(), tenantRuleItem.getRewritableSqlTypes());
        assertEquals(configItem.getTableNames(), tenantRuleItem.getTableNames());
        assertEquals(configItem.getColumnName(), tenantRuleItem.getColumnName());
        assertEquals(configItem.getInsertColumnValue(), tenantRuleItem.getInsertColumnValue());
        assertEquals(configItem.getDeleteConditionColumnValue(), tenantRuleItem.getDeleteConditionColumnValue());
        assertEquals(configItem.getUpdateConditionColumnValue(), tenantRuleItem.getUpdateConditionColumnValue());
        assertEquals(configItem.getSelectConditionColumnValue(), tenantRuleItem.getSelectConditionColumnValue());
        assertEquals(configItem.getPriority(), tenantRuleItem.getPriority());
        assertNotNull(tenantRuleItem.getRules());
        assertTrue(tenantRuleItem.getRules().size() > 0);
    }

    @Test
    public void testMatchSelectStatement() throws JSQLParserException {
        String sql = "SELECT * FROM user";
        Statement statement = CCJSqlParserUtil.parse(sql);

        assertTrue(tenantRuleItem.match(statement));
    }

    @Test
    public void testMatchInsertStatement() throws JSQLParserException {
        String sql = "INSERT INTO user (name) VALUES ('test')";
        Statement statement = CCJSqlParserUtil.parse(sql);

        assertTrue(tenantRuleItem.match(statement));
    }

    @Test
    public void testMatchUpdateStatement() throws JSQLParserException {
        String sql = "UPDATE user SET name = 'updated' WHERE id = 1";
        Statement statement = CCJSqlParserUtil.parse(sql);

        assertTrue(tenantRuleItem.match(statement));
    }

    @Test
    public void testMatchDeleteStatement() throws JSQLParserException {
        String sql = "DELETE FROM user WHERE id = 1";
        Statement statement = CCJSqlParserUtil.parse(sql);

        assertTrue(tenantRuleItem.match(statement));
    }

    @Test
    public void testNoMatchNonTargetTable() throws JSQLParserException {
        String sql = "SELECT * FROM non_target_table";
        Statement statement = CCJSqlParserUtil.parse(sql);
        //仅匹配sql类型，不匹配表名
        assertTrue(tenantRuleItem.match(statement));
    }

    @Test
    public void testNoMatchUnsupportedSqlType() {
        // 创建只支持SELECT的配置项
        TenantConfig.ConfigItem selectOnlyConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("user"), "tenant_id", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig selectOnlyConfig = new TenantConfig(Collections.singletonList(selectOnlyConfigItem));
        TenantRule selectOnlyRule = new TenantRule(selectOnlyConfig);
        TenantRule.TenantRuleItem selectOnlyRuleItem = selectOnlyRule.getTenantRuleItems().get(0);

        try {
            // 测试INSERT语句，应该不匹配
            String insertSql = "INSERT INTO user (name) VALUES ('test')";
            Statement insertStatement = CCJSqlParserUtil.parse(insertSql);

            assertFalse(selectOnlyRuleItem.match(insertStatement));

            // 测试UPDATE语句，应该不匹配
            String updateSql = "UPDATE user SET name = 'updated' WHERE id = 1";
            Statement updateStatement = CCJSqlParserUtil.parse(updateSql);

            assertFalse(selectOnlyRuleItem.match(updateStatement));

            // 测试DELETE语句，应该不匹配
            String deleteSql = "DELETE FROM user WHERE id = 1";
            Statement deleteStatement = CCJSqlParserUtil.parse(deleteSql);

            assertFalse(selectOnlyRuleItem.match(deleteStatement));

            // 测试SELECT语句，应该匹配
            String selectSql = "SELECT * FROM user";
            Statement selectStatement = CCJSqlParserUtil.parse(selectSql);

            assertTrue(selectOnlyRuleItem.match(selectStatement));
        } catch (JSQLParserException e) {
            fail("不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testApplySelectStatement() throws JSQLParserException {
        String sql = "SELECT * FROM user";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        String originalSql = select.toString();
        tenantRuleItem.apply(select);
        String modifiedSql = select.toString();

        // SQL应该被修改
        assertNotEquals(originalSql, modifiedSql);
        // 应该包含租户条件
        assertTrue(modifiedSql.contains("tenant_id"));
    }

    @Test
    public void testApplyInsertStatement() throws JSQLParserException {
        String sql = "INSERT INTO user (name) VALUES ('test')";
        Insert insert = (Insert) CCJSqlParserUtil.parse(sql);

        String originalSql = insert.toString();
        tenantRuleItem.apply(insert);
        String modifiedSql = insert.toString();

        // SQL应该被修改
        assertNotEquals(originalSql, modifiedSql);
        // 应该包含租户字段
        assertTrue(modifiedSql.contains("tenant_id"));
    }

    @Test
    public void testApplyUpdateStatement() throws JSQLParserException {
        String sql = "UPDATE user SET name = 'updated' WHERE id = 1";
        Update update = (Update) CCJSqlParserUtil.parse(sql);

        String originalSql = update.toString();
        tenantRuleItem.apply(update);
        String modifiedSql = update.toString();

        // SQL应该被修改
        assertNotEquals(originalSql, modifiedSql);
        // 应该包含租户条件
        assertTrue(modifiedSql.contains("tenant_id"));
    }

    @Test
    public void testApplyDeleteStatement() throws JSQLParserException {
        String sql = "DELETE FROM user WHERE id = 1";
        Delete delete = (Delete) CCJSqlParserUtil.parse(sql);

        String originalSql = delete.toString();
        tenantRuleItem.apply(delete);
        String modifiedSql = delete.toString();

        // SQL应该被修改
        assertNotEquals(originalSql, modifiedSql);
        // 应该包含租户条件
        assertTrue(modifiedSql.contains("tenant_id"));
    }

    @Test
    public void testApplyNonTargetTable() throws JSQLParserException {
        String sql = "SELECT * FROM non_target_table";
        Select select = (Select) CCJSqlParserUtil.parse(sql);

        String originalSql = select.toString();
        tenantRuleItem.apply(select);
        String modifiedSql = select.toString();

        // SQL应该不被修改
        assertEquals(originalSql, modifiedSql);
    }

    @Test
    public void testMultipleConfigItems() {
        // 创建第二个配置项
        TenantConfig.ConfigItem secondConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("product"), "org_id", () -> "org-001", () -> "org-001", () -> "org-001", () -> "org-001", 20);

        TenantConfig multiItemConfig = new TenantConfig(Arrays.asList(configItem, secondConfigItem));
        TenantRule multiItemRule = new TenantRule(multiItemConfig);

        assertEquals(2, multiItemRule.getTenantRuleItems().size());
        assertEquals(configItem.getTableNames(), multiItemRule.getTenantRuleItems().get(0).getTableNames());
        assertEquals(secondConfigItem.getTableNames(), multiItemRule.getTenantRuleItems().get(1).getTableNames());

        // 规则数量应该是两个配置项规则之和
        assertTrue(multiItemRule.getRules().size() > tenantRule.getRules().size());
    }

    @Test
    public void testNullValues() {
        // 创建包含null值的配置项
        TenantConfig.ConfigItem configItemWithNulls = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("test_table"), "tenant_id", null,  // insertColumnValue
                null,  // deleteConditionColumnValue
                null,  // updateConditionColumnValue
                null,  // selectConditionColumnValue
                10);

        TenantConfig configWithNulls = new TenantConfig(Collections.singletonList(configItemWithNulls));
        TenantRule ruleWithNulls = new TenantRule(configWithNulls);

        assertNotNull(ruleWithNulls);
        assertNotNull(ruleWithNulls.getTenantRuleItems());
        assertEquals(1, ruleWithNulls.getTenantRuleItems().size());

        TenantRule.TenantRuleItem ruleItemWithNulls = ruleWithNulls.getTenantRuleItems().get(0);
        assertNull(ruleItemWithNulls.getInsertColumnValue());
        assertNull(ruleItemWithNulls.getDeleteConditionColumnValue());
        assertNull(ruleItemWithNulls.getUpdateConditionColumnValue());
        assertNull(ruleItemWithNulls.getSelectConditionColumnValue());
    }

    @Test
    public void testDifferentColumnTypes() {
        // 创建不同类型字段值的配置项
        TenantConfig.ConfigItem intConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("int_table"), "tenant_id", () -> 123, () -> 123, () -> 123, () -> 123, 10);

        TenantConfig.ConfigItem stringConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("string_table"), "tenant_code", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", () -> "tenant-001", 10);

        TenantConfig.ConfigItem boolConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("bool_table"), "is_active", () -> true, () -> true, () -> true, () -> true, 10);

        TenantConfig intConfig = new TenantConfig(Collections.singletonList(intConfigItem));
        TenantConfig stringConfig = new TenantConfig(Collections.singletonList(stringConfigItem));
        TenantConfig boolConfig = new TenantConfig(Collections.singletonList(boolConfigItem));

        TenantRule intRule = new TenantRule(intConfig);
        TenantRule stringRule = new TenantRule(stringConfig);
        TenantRule boolRule = new TenantRule(boolConfig);

        assertNotNull(intRule);
        assertNotNull(stringRule);
        assertNotNull(boolRule);

        assertEquals(Integer.valueOf(123), intRule.getTenantRuleItems().get(0).getInsertColumnValue());
        assertEquals("tenant-001", stringRule.getTenantRuleItems().get(0).getInsertColumnValue());
        assertEquals(Boolean.TRUE, boolRule.getTenantRuleItems().get(0).getInsertColumnValue());
    }

    @Test
    public void testRulePriority() {
        // 创建不同优先级的配置项
        TenantConfig.ConfigItem highPriorityConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("high_priority_table"), "tenant_id", () -> "high-tenant", () -> "high-tenant", () -> "high-tenant", () -> "high-tenant", 1);

        TenantConfig.ConfigItem lowPriorityConfigItem = new TenantConfig.ConfigItem(Collections.singletonList(SQLTypeEnum.SELECT), Collections.singletonList("low_priority_table"), "tenant_id", () -> "low-tenant", () -> "low-tenant", () -> "low-tenant", () -> "low-tenant", 100);

        TenantConfig highPriorityConfig = new TenantConfig(Collections.singletonList(highPriorityConfigItem));
        TenantConfig lowPriorityConfig = new TenantConfig(Collections.singletonList(lowPriorityConfigItem));

        TenantRule highPriorityRule = new TenantRule(highPriorityConfig);
        TenantRule lowPriorityRule = new TenantRule(lowPriorityConfig);

        assertTrue(highPriorityRule.getTenantRuleItems().get(0).getPriority() < lowPriorityRule.getTenantRuleItems().get(0).getPriority());
        assertEquals(1, highPriorityRule.getTenantRuleItems().get(0).getPriority());
        assertEquals(100, lowPriorityRule.getTenantRuleItems().get(0).getPriority());
    }

    @Test
    public void testComplexSqlWithJoin() throws JSQLParserException {
        String complexSql = "SELECT u.*, o.order_id FROM user u JOIN order o ON u.id = o.user_id WHERE u.status = 'active'";
        Select select = (Select) CCJSqlParserUtil.parse(complexSql);

        String originalSql = select.toString();
        tenantRuleItem.apply(select);
        String modifiedSql = select.toString();

        // SQL应该被修改
        assertNotEquals(originalSql, modifiedSql);
        // 应该包含租户条件
        assertTrue(modifiedSql.contains("tenant_id"));
    }

    @Test
    public void testSqlWithSubquery() throws JSQLParserException {
        String subquerySql = "SELECT * FROM user WHERE id IN (SELECT user_id FROM order WHERE total > 100)";
        Select select = (Select) CCJSqlParserUtil.parse(subquerySql);

        String originalSql = select.toString();
        tenantRuleItem.apply(select);
        String modifiedSql = select.toString();

        // SQL应该被修改
        assertNotEquals(originalSql, modifiedSql);
        // 应该包含租户条件
        assertTrue(modifiedSql.contains("tenant_id"));
    }
}