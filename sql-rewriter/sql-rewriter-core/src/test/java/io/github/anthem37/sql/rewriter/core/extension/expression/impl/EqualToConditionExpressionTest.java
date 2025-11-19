package io.github.anthem37.sql.rewriter.core.extension.expression.impl;

import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * EqualToConditionExpression 单元测试
 */
public class EqualToConditionExpressionTest {

    // ========== 基础功能测试 ==========

    @Test
    public void testConstructorWithStringValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        assertEquals("tenant", expression.getTableName());
        assertEquals("tenant_id", expression.getColumnName());
        assertEquals("TENANT_1", expression.getColumnValue());
        assertEquals("tenant.tenant_id = 'TENANT_1'", expression.toString());
    }

    @Test
    public void testConstructorWithNumericValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "id", 123);

        assertEquals("user", expression.getTableName());
        assertEquals("id", expression.getColumnName());
        assertEquals(123, expression.getColumnValue());
        assertEquals("user.id = 123", expression.toString());
    }

    @Test
    public void testConstructorWithDecimalValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("product", "price", 99.99);

        assertEquals("product", expression.getTableName());
        assertEquals("price", expression.getColumnName());
        assertEquals(99.99, expression.getColumnValue());
        assertEquals("product.price = 99.99", expression.toString());
    }

    @Test
    public void testConstructorWithBooleanValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "is_active", true);

        assertEquals("user", expression.getTableName());
        assertEquals("is_active", expression.getColumnName());
        assertEquals(true, expression.getColumnValue());
        assertEquals("user.is_active = 'true'", expression.toString());
    }

    @Test
    public void testConstructorWithNullValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "deleted_at", null);

        assertEquals("user", expression.getTableName());
        assertEquals("deleted_at", expression.getColumnName());
        assertNull(expression.getColumnValue());
        assertEquals("user.deleted_at = NULL", expression.toString());
    }

    // ========== 数据类型测试 ==========

    @Test
    public void testWithBigDecimalValue() {
        BigDecimal value = new BigDecimal("12345.6789");
        EqualToConditionExpression expression = new EqualToConditionExpression("account", "balance", value);

        assertEquals(value, expression.getColumnValue());
        assertEquals("account.balance = '12345.6789'", expression.toString());
    }

    @Test
    public void testWithZeroValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("counter", "value", 0);

        assertEquals(0, expression.getColumnValue());
        assertEquals("counter.value = 0", expression.toString());
    }

    @Test
    public void testWithEmptyString() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "name", "");

        assertEquals("", expression.getColumnValue());
        assertEquals("user.name = ''", expression.toString());
    }

    @Test
    public void testWithDateValue() {
        Date now = new Date();
        EqualToConditionExpression expression = new EqualToConditionExpression("log", "created_at", now);

        assertEquals(now, expression.getColumnValue());
        // 日期格式的具体输出可能因JSQLParser版本而异，这里只验证包含时间
        assertTrue(expression.toString().contains("log.created_at"));
        assertTrue(expression.toString().contains("="));
    }

    // ========== 别名重构测试 ==========

    @Test
    public void testReconstructAliasExpressionWithSimpleAlias() {
        EqualToConditionExpression original = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");
        EqualToConditionExpression aliased = (EqualToConditionExpression) original.reconstructAliasExpression("t");

        assertEquals("t", aliased.getTableName());
        assertEquals("tenant_id", aliased.getColumnName());
        assertEquals("TENANT_1", aliased.getColumnValue());
        assertEquals("t.tenant_id = 'TENANT_1'", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithComplexAlias() {
        EqualToConditionExpression original = new EqualToConditionExpression("user", "id", 123);
        EqualToConditionExpression aliased = (EqualToConditionExpression) original.reconstructAliasExpression("u_123");

        assertEquals("u_123", aliased.getTableName());
        assertEquals("id", aliased.getColumnName());
        assertEquals(123, aliased.getColumnValue());
        assertEquals("u_123.id = 123", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithEmptyAlias() {
        EqualToConditionExpression original = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");
        EqualToConditionExpression aliased = (EqualToConditionExpression) original.reconstructAliasExpression("");

        assertEquals("", aliased.getTableName());
        assertEquals("tenant_id", aliased.getColumnName());
        assertEquals("TENANT_1", aliased.getColumnValue());
        assertEquals("tenant_id = 'TENANT_1'", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithNullAlias() {
        EqualToConditionExpression original = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");
        EqualToConditionExpression aliased = (EqualToConditionExpression) original.reconstructAliasExpression(null);

        assertNull(aliased.getTableName());
        assertEquals("tenant_id", aliased.getColumnName());
        assertEquals("TENANT_1", aliased.getColumnValue());
        assertEquals("tenant_id = 'TENANT_1'", aliased.toString());
    }

    // ========== 边界条件测试 ==========

    @Test
    public void testWithSpecialCharactersInValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "name", "O'Reilly");

        assertEquals("user.name = 'O'Reilly'", expression.toString());
    }

    @Test
    public void testWithUnicodeCharacters() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "name", "测试用户");

        assertEquals("user.name = '测试用户'", expression.toString());
    }

    @Test
    public void testWithNewlineCharacter() {
        EqualToConditionExpression expression = new EqualToConditionExpression("log", "message", "Line 1\nLine 2");

        assertEquals("log.message = 'Line 1\nLine 2'", expression.toString());
    }

    @Test
    public void testWithQuoteCharacters() {
        EqualToConditionExpression expression = new EqualToConditionExpression("text", "content", "He said \"Hello\"");

        assertEquals("text.content = 'He said \"Hello\"'", expression.toString());
    }

    @Test
    public void testWithNegativeNumber() {
        EqualToConditionExpression expression = new EqualToConditionExpression("balance", "amount", -100.50);

        assertEquals(-100.50, expression.getColumnValue());
        assertEquals("balance.amount = -100.5", expression.toString());
    }

    // ========== 继承相关测试 ==========

    @Test
    public void testIsExpressionType() {
        EqualToConditionExpression expression = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        // 验证继承关系
        assertTrue(expression instanceof net.sf.jsqlparser.expression.Expression);
        assertTrue(expression instanceof net.sf.jsqlparser.expression.operators.relational.EqualsTo);
        assertTrue(expression instanceof IConditionExpression);
    }

    @Test
    public void testGetLeftExpression() {
        EqualToConditionExpression expression = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        assertNotNull(expression.getLeftExpression());
        assertEquals("tenant.tenant_id", expression.getLeftExpression().toString());
    }

    @Test
    public void testGetRightExpression() {
        EqualToConditionExpression expression = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        assertNotNull(expression.getRightExpression());
        assertEquals("'TENANT_1'", expression.getRightExpression().toString());
    }

    @Test
    public void testGetRightExpressionWithNumericValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "id", 123);

        assertNotNull(expression.getRightExpression());
        assertEquals("123", expression.getRightExpression().toString());
    }

    // ========== 实际使用场景测试 ==========

    @Test
    public void testMultiTenantScenario() {
        // 多租户场景
        EqualToConditionExpression expression = new EqualToConditionExpression("order", "tenant_id", "company_001");

        assertEquals("order.tenant_id = 'company_001'", expression.toString());
    }

    @Test
    public void testUserAuthenticationScenario() {
        // 用户认证场景
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "username", "john.doe");

        assertEquals("user.username = 'john.doe'", expression.toString());
    }

    @Test
    public void testStatusFilterScenario() {
        // 状态过滤场景
        EqualToConditionExpression expression = new EqualToConditionExpression("order", "status", "PROCESSING");

        assertEquals("order.status = 'PROCESSING'", expression.toString());
    }

    @Test
    public void testPermissionCheckScenario() {
        // 权限检查场景
        EqualToConditionExpression expression = new EqualToConditionExpression("permission", "role_id", 5);

        assertEquals("permission.role_id = 5", expression.toString());
    }

    @Test
    public void testSoftDeleteScenario() {
        // 软删除场景（检查是否未删除）
        EqualToConditionExpression expression = new EqualToConditionExpression("user", "deleted", false);

        assertEquals("user.deleted = 'false'", expression.toString());
    }

    // ========== 等价性测试 ==========

    @Test
    public void testEqualityBasedOnContent() {
        EqualToConditionExpression expr1 = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");
        EqualToConditionExpression expr2 = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        // 注意：这里测试toString等价性，因为EqualsTo没有重写equals方法
        assertEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInTableName() {
        EqualToConditionExpression expr1 = new EqualToConditionExpression("tenant", "id", 1);
        EqualToConditionExpression expr2 = new EqualToConditionExpression("user", "id", 1);

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInColumnName() {
        EqualToConditionExpression expr1 = new EqualToConditionExpression("tenant", "id", 1);
        EqualToConditionExpression expr2 = new EqualToConditionExpression("tenant", "user_id", 1);

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInValue() {
        EqualToConditionExpression expr1 = new EqualToConditionExpression("tenant", "status", "ACTIVE");
        EqualToConditionExpression expr2 = new EqualToConditionExpression("tenant", "status", "INACTIVE");

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    // ========== 复杂场景测试 ==========

    @Test
    public void testWithTableAliasInRealQuery() {
        // 模拟实际查询中的使用场景
        EqualToConditionExpression original = new EqualToConditionExpression("user", "tenant_id", "TENANT_1");
        EqualToConditionExpression withAlias = (EqualToConditionExpression) original.reconstructAliasExpression("u");

        String expectedQueryPart = "u.tenant_id = 'TENANT_1'";
        assertEquals(expectedQueryPart, withAlias.toString());

        // 验证可以在复杂查询中使用
        String fullQuery = "SELECT u.* FROM user u WHERE " + withAlias.toString();
        assertEquals("SELECT u.* FROM user u WHERE u.tenant_id = 'TENANT_1'", fullQuery);
    }

    @Test
    public void testConsistentBehaviorAcrossDataTypes() {
        // 验证不同数据类型的行为一致性
        EqualToConditionExpression stringExpr = new EqualToConditionExpression("table", "col_string", "test");
        EqualToConditionExpression intExpr = new EqualToConditionExpression("table", "col_int", 123);
        EqualToConditionExpression doubleExpr = new EqualToConditionExpression("table", "col_double", 45.67);
        EqualToConditionExpression boolExpr = new EqualToConditionExpression("table", "col_bool", true);
        EqualToConditionExpression nullExpr = new EqualToConditionExpression("table", "col_null", null);

        assertEquals("table.col_string = 'test'", stringExpr.toString());
        assertEquals("table.col_int = 123", intExpr.toString());
        assertEquals("table.col_double = 45.67", doubleExpr.toString());
        assertEquals("table.col_bool = 'true'", boolExpr.toString());
        assertEquals("table.col_null = NULL", nullExpr.toString());
    }

    // ========== 接口实现测试 ==========

    @Test
    public void testReconstructAliasExpressionReturnsCorrectType() {
        EqualToConditionExpression original = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        // 验证返回类型正确
        IConditionExpression result = original.reconstructAliasExpression("t");

        assertTrue(result instanceof EqualToConditionExpression);
        assertEquals("t.tenant_id = 'TENANT_1'", result.toString());
    }

    @Test
    public void testDefaultBehaviorOfReconstructAliasExpression() {
        // 测试接口的默认行为（当前类重写了该方法）
        EqualToConditionExpression original = new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1");

        // 当前实现重写了reconstructAliasExpression，应该返回新实例
        IConditionExpression result = original.reconstructAliasExpression("t");

        assertNotSame(original, result); // 应该是不同的实例
        assertEquals("t.tenant_id = 'TENANT_1'", result.toString());
    }

    // ========== 边界值测试 ==========

    @Test
    public void testWithVeryLargeNumber() {
        Long largeNumber = Long.MAX_VALUE;
        EqualToConditionExpression expression = new EqualToConditionExpression("counter", "big_value", largeNumber);

        assertEquals(largeNumber, expression.getColumnValue());
        assertTrue(expression.toString().contains("big_value ="));
        assertTrue(expression.toString().contains(String.valueOf(largeNumber)));
    }

    @Test
    public void testWithVerySmallNumber() {
        Double smallNumber = Double.MIN_VALUE;
        EqualToConditionExpression expression = new EqualToConditionExpression("precise", "tiny_value", smallNumber);

        assertEquals(smallNumber, expression.getColumnValue());
        assertTrue(expression.toString().contains("tiny_value ="));
    }

    @Test
    public void testWithInfinityValue() {
        EqualToConditionExpression expression = new EqualToConditionExpression("measurement", "value", Double.POSITIVE_INFINITY);

        assertEquals(Double.POSITIVE_INFINITY, expression.getColumnValue());
        // 具体输出格式可能因JSQLParser处理而异
        assertTrue(expression.toString().contains("measurement.value"));
    }
}