package io.github.anthem37.sql.rewiter.core.extension.rule;

import io.github.anthem37.sql.rewiter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewiter.core.extension.expression.impl.IsBooleanConditionExpression;
import io.github.anthem37.sql.rewiter.core.extension.expression.impl.IsNullConditionExpression;
import io.github.anthem37.sql.rewiter.core.rule.RulePriority;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * AddConditionSelectRule 单元测试 - 覆盖所有常用SELECT场景
 */
public class AddConditionSelectRuleTest {

    // ========== 辅助方法 ==========

    private AddConditionSelectRule createTenantRule() {
        return new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
    }

    private AddConditionSelectRule createOrdersRule() {
        return new AddConditionSelectRule("orders", new EqualToConditionExpression("orders", "tenant_id", "TENANT_1"));
    }

    // ========== 基础SELECT场景 ==========

    @Test
    public void testBasicSelectAll() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithSpecificColumns() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT id, name, status FROM tenant");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithTableAlias() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("t.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithExistingWhere() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant WHERE status = 'ACTIVE'");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        AndExpression where = (AndExpression) plainSelect.getWhere();
        assertEquals("status = 'ACTIVE'", ((Parenthesis) where.getLeftExpression()).getExpression().toString());
        assertEquals("tenant.tenant_id = 'TENANT_1'", where.getRightExpression().toString());
    }

    @Test
    public void testSelectWithDistinct() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT DISTINCT name FROM tenant");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithOrderBy() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant ORDER BY created_at DESC");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertNotNull(plainSelect.getOrderByElements());
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithLimit() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant LIMIT 10");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertNotNull(plainSelect.getLimit());
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    // ========== JOIN场景 ==========

    @Test
    public void testInnerJoin() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t INNER JOIN orders o ON t.id = o.tenant_id");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        assertEquals(1, joins.size());
        AndExpression onExpression = (AndExpression) joins.get(0).getOnExpression();
        assertEquals("o.tenant_id = 'TENANT_1'", onExpression.getRightExpression().toString());
    }

    @Test
    public void testLeftJoin() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t LEFT JOIN orders o ON t.id = o.tenant_id");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        assertTrue(joins.get(0).isLeft());
        AndExpression onExpression = (AndExpression) joins.get(0).getOnExpression();
        assertEquals("o.tenant_id = 'TENANT_1'", onExpression.getRightExpression().toString());
    }

    @Test
    public void testRightJoin() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t RIGHT JOIN orders o ON t.id = o.tenant_id");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        assertTrue(joins.get(0).isRight());
        AndExpression onExpression = (AndExpression) joins.get(0).getOnExpression();
        assertEquals("o.tenant_id = 'TENANT_1'", onExpression.getRightExpression().toString());
    }

    @Test
    public void testCrossJoin() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t CROSS JOIN orders o");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("o.tenant_id = 'TENANT_1'", plainSelect.getJoins().get(0).getOnExpression().toString());
    }

    @Test
    public void testMultipleJoins() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t " + "JOIN orders o ON t.id = o.tenant_id " + "LEFT JOIN products p ON o.product_id = p.id");
        Select select = (Select) statement;
        new AddConditionSelectRule("products", new EqualToConditionExpression("products", "tenant_id", "TENANT_1")).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        assertEquals(2, joins.size());

        Join productsJoin = joins.get(1);
        AndExpression onExpression = (AndExpression) productsJoin.getOnExpression();
        assertEquals("p.tenant_id = 'TENANT_1'", onExpression.getRightExpression().toString());
    }

    // ========== 子查询场景 ==========

    @Test
    public void testSubqueryInFrom() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM (SELECT id, name FROM tenant WHERE status = 'ACTIVE') t");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect outerSelect = (PlainSelect) select.getSelectBody();
        FromItem fromItem = outerSelect.getFromItem();
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
        PlainSelect innerSelect = (PlainSelect) parenthesedSelect.getSelect().getSelectBody();

        AndExpression where = (AndExpression) innerSelect.getWhere();
        assertEquals("tenant.tenant_id = 'TENANT_1'", where.getRightExpression().toString());
    }

    @Test
    public void testNestedSubquery() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM (SELECT * FROM (SELECT * FROM tenant) t) nested");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect outerSelect = (PlainSelect) select.getSelectBody();
        FromItem fromItem = outerSelect.getFromItem();
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
        PlainSelect middleSelect = (PlainSelect) parenthesedSelect.getSelect().getSelectBody();
        FromItem middleFromItem = middleSelect.getFromItem();
        ParenthesedSelect innerParenthesedSelect = (ParenthesedSelect) middleFromItem;
        PlainSelect innerSelect = (PlainSelect) innerParenthesedSelect.getSelect().getSelectBody();

        assertEquals("tenant.tenant_id = 'TENANT_1'", innerSelect.getWhere().toString());
    }

    @Test
    public void testSubqueryInJoin() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t " + "JOIN (SELECT * FROM orders WHERE amount > 100) o ON t.id = o.tenant_id");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        List<Join> joins = plainSelect.getJoins();
        FromItem rightItem = joins.get(0).getRightItem();
        ParenthesedSelect parenthesedSelect = (ParenthesedSelect) rightItem;
        PlainSelect innerSelect = (PlainSelect) parenthesedSelect.getSelect().getSelectBody();

        AndExpression where = (AndExpression) innerSelect.getWhere();
        assertEquals("orders.tenant_id = 'TENANT_1'", where.getRightExpression().toString());
    }

    @Test
    public void testSubqueryInWhereExists() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t WHERE EXISTS (SELECT 1 FROM orders o WHERE o.tenant_id = t.id)");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        String rewrittenSql = select.toString();
        assertTrue(rewrittenSql.contains("o.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testSubqueryInWhereIn() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t WHERE t.id IN (SELECT tenant_id FROM orders WHERE amount > 100)");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        String rewrittenSql = select.toString();
        assertTrue(rewrittenSql.contains("orders.tenant_id = 'TENANT_1'"));
    }

    // ========== 聚合查询 ==========

    @Test
    public void testSelectWithGroupBy() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT tenant_id, COUNT(*) as order_count FROM orders GROUP BY tenant_id");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertNotNull(plainSelect.getGroupBy());
        assertEquals("orders.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithHaving() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT tenant_id, COUNT(*) as order_count FROM orders GROUP BY tenant_id HAVING COUNT(*) > 5");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertNotNull(plainSelect.getGroupBy());
        assertNotNull(plainSelect.getHaving());
        assertEquals("orders.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithAggregateFunctions() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT COUNT(*), SUM(amount), AVG(amount) FROM orders");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("orders.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSelectWithWindowFunction() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT id, name, ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY created_at) as rn FROM orders");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("orders.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    // ========== 高级特性 ==========

    @Test
    public void testCTEQuery() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("WITH tenant_stats AS (SELECT tenant_id, COUNT(*) FROM orders GROUP BY tenant_id) " + "SELECT t.*, ts.order_count FROM tenant t LEFT JOIN tenant_stats ts ON t.id = ts.tenant_id");
        Select select = (Select) statement;
        createOrdersRule().applyTyped(select);

        String rewrittenSql = select.toString();
        assertTrue(rewrittenSql.contains("orders.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void testUnionQuery() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT id, name FROM tenant WHERE type = 'ACTIVE' " + "UNION " + "SELECT id, name FROM tenant WHERE type = 'PENDING'");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        String rewrittenSql = select.toString();
        int occurrences = 0;
        int index = 0;
        while ((index = rewrittenSql.indexOf("tenant.tenant_id = 'TENANT_1'", index)) != -1) {
            occurrences++;
            index += "tenant.tenant_id = 'TENANT_1'".length();
        }
        assertEquals(2, occurrences);
    }

    @Test
    public void testCaseExpression() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT id, CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END as is_active FROM tenant");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertNotNull(plainSelect.getWhere());
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    // ========== 表名和别名变体 ==========

    @Test
    public void testQuotedTableName() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM \"tenant\"");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("\"tenant\".tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testBacktickTableName() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM `tenant`");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("`tenant`.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSchemaQualifiedTable() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM public.tenant");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        //系统支持schema限定表名，应该正确匹配并添加条件
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testQuotedSchemaQualifiedTable() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM \"public\".\"tenant\"");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        //系统支持schema限定表名，应该正确匹配并添加条件
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("\"tenant\".tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSpecialAlias() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant \"t-alias\"");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("\"t-alias\".tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    // ========== 条件值类型 ==========

    @Test
    public void testNullValue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new IsNullConditionExpression("tenant", "tenant_id")).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.tenant_id IS NULL", plainSelect.getWhere().toString());
    }

    @Test
    public void testEmptyString() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "name", "")).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.name = ''", plainSelect.getWhere().toString());
    }

    @Test
    public void testNumericValue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "id", 123)).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.id = 123", plainSelect.getWhere().toString());
    }

    @Test
    public void testDecimalValue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "balance", 99.99)).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.balance = 99.99", plainSelect.getWhere().toString());
    }

    @Test
    public void testBooleanValue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new IsBooleanConditionExpression("tenant", "is_active", false, true)).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.is_active IS TRUE", plainSelect.getWhere().toString());
    }

    @Test
    public void testBooleanValueIsFalse() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new IsBooleanConditionExpression("tenant", "is_active", false, false)).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.is_active IS FALSE", plainSelect.getWhere().toString());
    }

    @Test
    public void testBooleanValueIsNotTrue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new IsBooleanConditionExpression("tenant", "is_active", true, true)).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.is_active IS NOT TRUE", plainSelect.getWhere().toString());
    }

    @Test
    public void testBooleanValueIsNotFalse() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new IsBooleanConditionExpression("tenant", "is_active", true, false)).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.is_active IS NOT FALSE", plainSelect.getWhere().toString());
    }

    // ========== 边界条件 ==========

    @Test
    public void testTableNotMatch() throws Exception {
        String originalSql = "SELECT * FROM orders";
        Statement statement = CCJSqlParserUtil.parse(originalSql);
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        assertEquals(originalSql, select.toString());
    }

    @Test
    public void testSelectWithoutFrom() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT 1");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        assertEquals("SELECT 1", select.toString());
    }

    @Test
    public void testEmptyAlias() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant AS \"\"");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("\"\".tenant_id = 'TENANT_1'", plainSelect.getWhere().toString());
    }

    @Test
    public void testDateTimeValue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "created_at", "2023-01-01 00:00:00")).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.created_at = '2023-01-01 00:00:00'", plainSelect.getWhere().toString());
    }

    @Test
    public void testSpecialCharactersInValue() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant");
        Select select = (Select) statement;
        new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "name", "O'Reilly")).applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        assertEquals("tenant.name = 'O'Reilly'", plainSelect.getWhere().toString());
    }

    @Test
    public void testComplexWhereClause() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant WHERE (status = 'ACTIVE' OR status = 'PENDING') AND created_at > '2023-01-01'");
        Select select = (Select) statement;
        createTenantRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        AndExpression where = (AndExpression) plainSelect.getWhere();
        assertEquals("tenant.tenant_id = 'TENANT_1'", where.getRightExpression().toString());
    }

    @Test
    public void testMultipleRuleApplication() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM tenant t JOIN orders o ON t.id = o.tenant_id");
        Select select = (Select) statement;

        // 先应用租户规则
        createTenantRule().applyTyped(select);
        // 再应用订单规则
        createOrdersRule().applyTyped(select);

        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        // 验证WHERE条件包含租户规则
        assertTrue(plainSelect.getWhere().toString().contains("t.tenant_id = 'TENANT_1'"));

        List<Join> joins = plainSelect.getJoins();
        // 验证JOIN条件包含订单规则
        assertTrue(joins.get(0).getOnExpression().toString().contains("o.tenant_id = 'TENANT_1'"));
    }

    // ========== 规则属性 ==========

    @Test
    public void testGetType() {
        AddConditionSelectRule rule = createTenantRule();
        assertSame(Select.class, rule.getType());
    }

    @Test
    public void testGetPriority() {
        AddConditionSelectRule rule = createTenantRule();
        assertEquals(RulePriority.SELECT_DEFAULT, rule.getPriority());

        AddConditionSelectRule ruleWithPriority = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"), 5);
        assertEquals(5, ruleWithPriority.getPriority());
    }

    @Test
    public void testGetTargetTableName() {
        AddConditionSelectRule rule = createTenantRule();
        assertEquals("tenant", rule.getTargetTableName());
    }

}