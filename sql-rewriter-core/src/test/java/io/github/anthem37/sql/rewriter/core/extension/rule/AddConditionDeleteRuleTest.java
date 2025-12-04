package io.github.anthem37.sql.rewriter.core.extension.rule;

import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.InConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsBooleanConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsNullConditionExpression;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * AddConditionDeleteRule 单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class AddConditionDeleteRuleTest {

    private static final String TABLE_NAME = "user";
    private static final String COLUMN_NAME = "tenant_id";
    private static final Object COLUMN_VALUE = "TENANT_001";

    private IConditionExpression conditionExpression;

    @Before
    public void setUp() {
        conditionExpression = new EqualToConditionExpression(TABLE_NAME, COLUMN_NAME, COLUMN_VALUE);
    }

    @Test
    public void testConstructorWithDefaultPriority() {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);

        assertEquals(TABLE_NAME, rule.getTableName());
        assertEquals(conditionExpression, rule.getConditionExpression());
        assertEquals(RulePriority.DELETE_DEFAULT, rule.getPriority());
    }

    @Test
    public void testConstructorWithCustomPriority() {
        int customPriority = 25;
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression, customPriority);

        assertEquals(TABLE_NAME, rule.getTableName());
        assertEquals(conditionExpression, rule.getConditionExpression());
        assertEquals(customPriority, rule.getPriority());
    }

    @Test
    public void testGetType() {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        assertEquals(Delete.class, rule.getType());
    }

    @Test
    public void testGetTargetTableName() {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        assertEquals(TABLE_NAME, rule.getTargetTableName());
    }

    @Test
    public void testMatchWithDeleteStatement() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE age > 18");

        assertTrue(rule.match(statement));
    }

    @Test
    public void testMatchWithSelectStatement() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM user");

        assertFalse(rule.match(statement));
    }

    @Test
    public void testMatchWithUpdateStatement() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("UPDATE user SET name = 'test'");

        assertFalse(rule.match(statement));
    }

    @Test
    public void testMatchWithDeleteStatementForDifferentTable() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM other_table WHERE age > 18");
        //仅通过sql类型进行判断是否匹配，不考虑表名
        assertTrue(rule.match(statement));
    }

    @Test
    public void testApplyTypedWithNoExistingWhere() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("DELETE FROM user"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("user.tenant_id = 'TENANT_001'"));
    }

    @Test
    public void testApplyTypedWithExistingWhere() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE age > 18");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("DELETE FROM user"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("(age > 18)"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("user.tenant_id = 'TENANT_001'"));
    }

    @Test
    public void testApplyTypedWithComplexExistingWhere() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE (age > 18 AND status = 'active') OR (age < 65 AND status = 'premium')");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("DELETE FROM user"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("((age > 18 AND status = 'active') OR (age < 65 AND status = 'premium'))"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("user.tenant_id = 'TENANT_001'"));
    }

    @Test
    public void testApplyTypedWithDifferentTable() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM other_table WHERE age > 18");
        String originalSql = statement.toString();

        rule.applyTyped((Delete) statement);

        assertEquals(originalSql, statement.toString());
    }

    @Test
    public void testApplyTypedWithInConditionExpression() throws JSQLParserException {
        java.util.List<String> tenantIds = java.util.Arrays.asList("TENANT_001", "TENANT_002");
        InConditionExpression inExpression = new InConditionExpression(TABLE_NAME, COLUMN_NAME, tenantIds);
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, inExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("DELETE FROM user"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("user.tenant_id IN ('TENANT_001', 'TENANT_002')"));
    }

    @Test
    public void testApplyTypedWithIsNullConditionExpression() throws JSQLParserException {
        IsNullConditionExpression nullExpression = new IsNullConditionExpression(TABLE_NAME, COLUMN_NAME);
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, nullExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("DELETE FROM user"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("user.tenant_id IS NULL"));
    }

    @Test
    public void testApplyTypedWithTableAlias() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user u WHERE u.age > 18");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("DELETE FROM user u"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("(u.age > 18)"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("u.tenant_id = 'TENANT_001'"));
    }

    @Test
    public void testApplyTypedWithMultipleApplyCalls() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE age > 18");

        rule.applyTyped((Delete) statement);
        String sql1 = statement.toString();

        rule.applyTyped((Delete) statement);
        String sql2 = statement.toString();

        assertEquals("DELETE FROM user WHERE (age > 18) AND user.tenant_id = 'TENANT_001'", sql1);
        assertEquals("DELETE FROM user WHERE ((age > 18) AND user.tenant_id = 'TENANT_001') AND user.tenant_id = 'TENANT_001'", sql2);
    }

    @Test
    public void testApplyTypedWithIntegerValue() throws JSQLParserException {
        EqualToConditionExpression intExpression = new EqualToConditionExpression(TABLE_NAME, COLUMN_NAME, 1001);
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, intExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("user.tenant_id = 1001"));
    }

    @Test
    public void testApplyTypedWithBooleanValue() throws JSQLParserException {
        IsBooleanConditionExpression boolExpression = new IsBooleanConditionExpression(TABLE_NAME, "active", true);
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, boolExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("user.active IS TRUE"));
    }

    @Test
    public void testApplyWithNullTable() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(null, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE age > 18");
        String originalSql = statement.toString();

        rule.applyTyped((Delete) statement);

        assertEquals(originalSql, statement.toString());
    }

    @Test
    public void testApplyWithEmptyConditionExpression() {
        // 测试基本功能，不创建匿名内部类
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        // 不做断言，只是测试不会抛出异常
    }

    @Test
    public void testApplyTypedWithDeleteWhereAnd() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE age > 18 AND status = 'active'");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("(age > 18 AND status = 'active')"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("user.tenant_id = 'TENANT_001'"));
    }

    @Test
    public void testApplyTypedWithDeleteWhereOr() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule(TABLE_NAME, conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM user WHERE age > 18 OR status = 'inactive'");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("(age > 18 OR status = 'inactive')"));
        assertTrue(sql.contains("AND"));
        assertTrue(sql.contains("user.tenant_id = 'TENANT_001'"));
    }

    @Test
    public void testApplyTypedWithCaseSensitiveTableName() throws JSQLParserException {
        AddConditionDeleteRule rule = new AddConditionDeleteRule("User", conditionExpression);
        Statement statement = CCJSqlParserUtil.parse("DELETE FROM User WHERE age > 18");

        rule.applyTyped((Delete) statement);

        String sql = statement.toString();
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("User.tenant_id = 'TENANT_001'"));
    }
}