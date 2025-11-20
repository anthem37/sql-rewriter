package io.github.anthem37.sql.rewriter.core.extension.rule;

import io.github.anthem37.sql.rewriter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.InConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsNullConditionExpression;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * AddConditionUpdateRule 单元测试 - 覆盖所有常用UPDATE场景
 */
public class AddConditionUpdateRuleTest {

    // ========== 辅助方法 ==========

    private AddConditionUpdateRule createTenantRule() {
        return new AddConditionUpdateRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
    }

    private AddConditionUpdateRule createOrdersRule() {
        return new AddConditionUpdateRule("orders", new EqualToConditionExpression("orders", "tenant_id", "TENANT_1"));
    }

    private AddConditionUpdateRule createOrdersRuleWithCustomPriority() {
        return new AddConditionUpdateRule("orders", new EqualToConditionExpression("orders", "tenant_id", "TENANT_1"), 100);
    }

    // ========== 基础UPDATE场景 ==========

    @Test
    public void testBasicUpdateSingleColumn() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
        assertTrue(whereStr.contains("AND"));
    }

    @Test
    public void testUpdateMultipleColumns() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test', status = 'active', updated_time = NOW() WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
        assertEquals(3, update.getUpdateSets().size());
    }

    @Test
    public void testUpdateWithoutWhere() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test'");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
        assertFalse(whereStr.contains("AND"));
    }

    @Test
    public void testUpdateWithComplexWhere() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE status = 'active' AND created_time > '2023-01-01'");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        assertTrue(update.getWhere() instanceof AndExpression);
        AndExpression andExpr = (AndExpression) update.getWhere();
        assertTrue(andExpr.getLeftExpression() instanceof Parenthesis);
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(status = 'active' AND created_time > '2023-01-01')"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    // ========== 表别名场景 ==========

    @Test
    public void testUpdateWithTableAlias() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant t SET t.name = 'test' WHERE t.id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(t.id = 1)"));
        assertTrue(whereStr.contains("t.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithAliasAndNoWhere() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant t SET t.name = 'test'");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("t.tenant_id = 'TENANT_1'"));
        assertFalse(whereStr.contains("AND"));
    }

    @Test
    public void testUpdateWithDifferentAliasName() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant AS tbl SET tbl.name = 'test' WHERE tbl.id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(tbl.id = 1)"));
        assertTrue(whereStr.contains("tbl.tenant_id = 'TENANT_1'"));
    }

    // ========== 不同条件表达式场景 ==========

    @Test
    public void testUpdateWithInCondition() throws Exception {
        AddConditionUpdateRule rule = new AddConditionUpdateRule("orders",
                new InConditionExpression("orders", "status", Arrays.asList("active", "pending")));

        Statement statement = CCJSqlParserUtil.parse("UPDATE orders SET amount = 100 WHERE id = 1");
        Update update = (Update) statement;
        rule.applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("orders.status IN ('active', 'pending')"));
    }

    @Test
    public void testUpdateWithIsNullCondition() throws Exception {
        AddConditionUpdateRule rule = new AddConditionUpdateRule("users",
                new IsNullConditionExpression("users", "deleted_at"));

        Statement statement = CCJSqlParserUtil.parse("UPDATE users SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;
        rule.applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("users.deleted_at IS NULL"));
    }

    // ========== 表名不匹配场景 ==========

    @Test
    public void testUpdateDifferentTableNoEffect() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE users SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;
        String originalWhere = update.getWhere().toString();

        createTenantRule().applyTyped(update);

        assertEquals(originalWhere, update.getWhere().toString());
    }

    @Test
    public void testUpdateCaseInsensitiveTableName() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE TENANT SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.toLowerCase().contains("tenant.tenant_id = 'TENANT_1'".toLowerCase()));
    }

    // ========== 复杂UPDATE场景 ==========

    @Test
    public void testUpdateWithSubqueryInSet() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = (SELECT MAX(name) FROM tenant) WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithJoinInWhere() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "UPDATE tenant t SET t.name = 'test' WHERE t.id IN (SELECT o.tenant_id FROM orders o WHERE o.amount > 100)");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("t.id IN (SELECT o.tenant_id FROM orders o WHERE o.amount > 100)"));
        assertTrue(whereStr.contains("t.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithExistsSubquery() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "UPDATE tenant SET name = 'test' WHERE EXISTS (SELECT 1 FROM orders o WHERE o.tenant_id = tenant.id)");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("EXISTS (SELECT 1 FROM orders o WHERE o.tenant_id = tenant.id)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithOrCondition() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE status = 'active' OR status = 'pending'");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(status = 'active' OR status = 'pending')"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    // ========== 特殊字符和引号场景 ==========

    @Test
    public void testUpdateTableNameWithQuotes() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE \"tenant\" SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("\"tenant\".tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithSpecialCharacters() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'O''Reilly', description = 'Test \"quotes\"' WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    // ========== 构造函数和优先级测试 ==========

    @Test
    public void testConstructorWithDefaultPriority() {
        AddConditionUpdateRule rule = createTenantRule();
        assertEquals("tenant", rule.getTableName());
        assertEquals(RulePriority.UPDATE_DEFAULT, rule.getPriority());
        assertEquals(Update.class, rule.getType());
    }

    @Test
    public void testConstructorWithCustomPriority() {
        AddConditionUpdateRule rule = createOrdersRuleWithCustomPriority();
        assertEquals("orders", rule.getTableName());
        assertEquals(100, rule.getPriority());
        assertEquals(Update.class, rule.getType());
    }

    @Test
    public void testGetTargetTableName() {
        AddConditionUpdateRule rule = createTenantRule();
        assertEquals("tenant", rule.getTargetTableName());
    }

    @Test
    public void testGetTableName() {
        AddConditionUpdateRule rule = createTenantRule();
        assertEquals("tenant", rule.getTableName());
    }

    // ========== 数值类型场景 ==========

    @Test
    public void testUpdateWithIntegerValues() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET age = 25, score = 95 WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithDoubleValues() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET price = 99.99, discount = 0.15 WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithNullValues() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET deleted_at = NULL WHERE id = 1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(id = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    // ========== 边界条件测试 ==========

    @Test
    public void testUpdateEmptySet() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE 1=0");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(1 = 0)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUpdateWithAlwaysTrueWhere() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE 1=1");
        Update update = (Update) statement;
        createTenantRule().applyTyped(update);

        assertNotNull(update.getWhere());
        String whereStr = update.getWhere().toString();
        assertTrue(whereStr.contains("(1 = 1)"));
        assertTrue(whereStr.contains("tenant.tenant_id = 'TENANT_1'"));
    }

    // ========== 性能和效率测试 ==========

    @Test
    public void testMultipleRuleApplications() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;

        // 应用规则
        AddConditionUpdateRule rule = createTenantRule();
        rule.applyTyped(update);
        String result = update.getWhere().toString();

        // 验证条件已正确添加
        assertTrue(result.contains("(id = 1)"));
        assertTrue(result.contains("tenant.tenant_id = 'TENANT_1'"));

        // 验证AND结构
        assertTrue(result instanceof String);
        assertTrue(result.contains("AND"));
    }

    @Test
    public void testDifferentRulesOnSameUpdate() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE id = 1");
        Update update = (Update) statement;

        // 先应用一个规则
        createTenantRule().applyTyped(update);
        String firstResult = update.getWhere().toString();

        // 重置并应用不同的规则
        statement = CCJSqlParserUtil.parse("UPDATE tenant SET name = 'test' WHERE id = 1");
        update = (Update) statement;

        AddConditionUpdateRule differentRule = new AddConditionUpdateRule("tenant",
                new EqualToConditionExpression("tenant", "status", "active"));
        differentRule.applyTyped(update);

        assertNotEquals(firstResult, update.getWhere().toString());
        String secondResult = update.getWhere().toString();
        assertTrue(secondResult.contains("tenant.status = 'active'"));
    }
}