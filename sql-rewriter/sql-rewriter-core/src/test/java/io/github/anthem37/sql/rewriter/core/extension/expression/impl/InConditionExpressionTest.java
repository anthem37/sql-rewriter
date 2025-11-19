package io.github.anthem37.sql.rewriter.core.extension.expression.impl;

import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * InConditionExpression 单元测试
 */
public class InConditionExpressionTest {

    // ========== 基础功能测试 ==========

    @Test
    public void testConstructorWithSingleValue() {
        InConditionExpression expression = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));

        assertEquals("tenant", expression.getTableName());
        assertEquals("status", expression.getColumnName());
        assertEquals(Arrays.asList("ACTIVE"), expression.getColumnValue());
        assertEquals("tenant.status IN ('ACTIVE')", expression.toString());
    }

    @Test
    public void testConstructorWithMultipleValues() {
        InConditionExpression expression = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE", "PENDING", "INACTIVE"));

        assertEquals("tenant", expression.getTableName());
        assertEquals("status", expression.getColumnName());
        assertEquals(Arrays.asList("ACTIVE", "PENDING", "INACTIVE"), expression.getColumnValue());
        assertEquals("tenant.status IN ('ACTIVE', 'PENDING', 'INACTIVE')", expression.toString());
    }

    @Test
    public void testConstructorWithEmptyList() {
        InConditionExpression expression = new InConditionExpression("tenant", "status", Collections.emptyList());

        assertEquals("tenant", expression.getTableName());
        assertEquals("status", expression.getColumnName());
        assertEquals(Collections.emptyList(), expression.getColumnValue());
        assertEquals("tenant.status IN ()", expression.toString());
    }

    // ========== 数据类型测试 ==========

    @Test
    public void testWithNumericValues() {
        InConditionExpression expression = new InConditionExpression("tenant", "id", Arrays.asList(1, 2, 3));

        assertEquals(Arrays.asList(1, 2, 3), expression.getColumnValue());
        assertEquals("tenant.id IN (1, 2, 3)", expression.toString());
    }

    @Test
    public void testWithDecimalValues() {
        InConditionExpression expression = new InConditionExpression("product", "price", Arrays.asList(10.5, 20.99, 30.0));

        assertEquals(Arrays.asList(10.5, 20.99, 30.0), expression.getColumnValue());
        assertEquals("product.price IN (10.5, 20.99, 30.0)", expression.toString());
    }

    @Test
    public void testWithBooleanValues() {
        InConditionExpression expression = new InConditionExpression("user", "is_active", Arrays.asList(true, false));

        assertEquals(Arrays.asList(true, false), expression.getColumnValue());
        assertEquals("user.is_active IN ('true', 'false')", expression.toString());
    }

    @Test
    public void testWithNullValue() {
        InConditionExpression expression = new InConditionExpression("tenant", "name", Arrays.asList("Test", null));

        assertEquals(Arrays.asList("Test", null), expression.getColumnValue());
        assertEquals("tenant.name IN ('Test', NULL)", expression.toString());
    }

    @Test
    public void testWithMixedTypeValues() {
        InConditionExpression expression = new InConditionExpression("data", "value", Arrays.asList("string", 123, 45.67, true));

        assertEquals(Arrays.asList("string", 123, 45.67, true), expression.getColumnValue());
        assertEquals("data.value IN ('string', 123, 45.67, 'true')", expression.toString());
    }

    // ========== 别名重构测试 ==========

    @Test
    public void testReconstructAliasExpressionWithSimpleAlias() {
        InConditionExpression original = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE", "PENDING"));
        InConditionExpression aliased = (InConditionExpression) original.reconstructAliasExpression("t");

        assertEquals("t", aliased.getTableName());
        assertEquals("status", aliased.getColumnName());
        assertEquals(Arrays.asList("ACTIVE", "PENDING"), aliased.getColumnValue());
        assertEquals("t.status IN ('ACTIVE', 'PENDING')", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithComplexAlias() {
        InConditionExpression original = new InConditionExpression("user", "id", Arrays.asList(1, 2, 3));
        InConditionExpression aliased = (InConditionExpression) original.reconstructAliasExpression("u_123");

        assertEquals("u_123", aliased.getTableName());
        assertEquals("id", aliased.getColumnName());
        assertEquals(Arrays.asList(1, 2, 3), aliased.getColumnValue());
        assertEquals("u_123.id IN (1, 2, 3)", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithEmptyAlias() {
        InConditionExpression original = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));
        InConditionExpression aliased = (InConditionExpression) original.reconstructAliasExpression("");

        assertEquals("", aliased.getTableName());
        assertEquals("status", aliased.getColumnName());
        assertEquals(Arrays.asList("ACTIVE"), aliased.getColumnValue());
        assertEquals("status IN ('ACTIVE')", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithNullAlias() {
        InConditionExpression original = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));
        InConditionExpression aliased = (InConditionExpression) original.reconstructAliasExpression(null);

        assertEquals(null, aliased.getTableName());
        assertEquals("status", aliased.getColumnName());
        assertEquals(Arrays.asList("ACTIVE"), aliased.getColumnValue());
        assertEquals("status IN ('ACTIVE')", aliased.toString());
    }

    // ========== 边界条件测试 ==========

    @Test
    public void testWithSpecialCharactersInValues() {
        InConditionExpression expression = new InConditionExpression("user", "name", Arrays.asList("O'Reilly", "John Doe", "test\nvalue"));

        assertEquals("user.name IN ('O'Reilly', 'John Doe', 'test\nvalue')", expression.toString());
    }

    @Test
    public void testWithUnicodeCharacters() {
        InConditionExpression expression = new InConditionExpression("tenant", "name", Arrays.asList("测试用户", "ユーザー", "пользователь"));

        assertEquals("tenant.name IN ('测试用户', 'ユーザー', 'пользователь')", expression.toString());
    }

    @Test
    public void testWithLargeValueList() {
        // 测试大量值的情况
        List<String> largeList = java.util.stream.IntStream.range(1, 101)
                .mapToObj(i -> "value_" + i)
                .collect(java.util.stream.Collectors.toList());

        InConditionExpression expression = new InConditionExpression("data", "category", largeList);

        assertEquals(largeList, expression.getColumnValue());
        assertTrue(expression.toString().startsWith("data.category IN ("));
        assertTrue(expression.toString().endsWith(")"));
        assertTrue(expression.toString().contains("value_1"));
        assertTrue(expression.toString().contains("value_100"));
    }

    // ========== 继承相关测试 ==========

    @Test
    public void testIsExpressionType() {
        InConditionExpression expression = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));

        // 验证继承关系
        assertTrue(expression instanceof net.sf.jsqlparser.expression.Expression);
        assertTrue(expression instanceof net.sf.jsqlparser.expression.operators.relational.InExpression);
        assertTrue(expression instanceof IConditionExpression);
    }

    @Test
    public void testGetLeftExpression() {
        InConditionExpression expression = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));

        assertNotNull(expression.getLeftExpression());
        assertEquals("tenant.status", expression.getLeftExpression().toString());
    }

    @Test
    public void testGetRightExpression() {
        InConditionExpression expression = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE", "PENDING"));

        assertNotNull(expression.getRightExpression());
        assertEquals("('ACTIVE', 'PENDING')", expression.getRightExpression().toString());
    }

    // ========== 实际使用场景测试 ==========

    @Test
    public void testInMultiTenantScenario() {
        // 多租户场景中的使用
        InConditionExpression expression = new InConditionExpression("tenant", "tenant_id", Arrays.asList("tenant_1", "tenant_2", "tenant_3"));

        assertEquals("tenant.tenant_id IN ('tenant_1', 'tenant_2', 'tenant_3')", expression.toString());
    }

    @Test
    public void testInStatusFilterScenario() {
        // 状态过滤场景
        InConditionExpression expression = new InConditionExpression("order", "status", Arrays.asList("PENDING", "PROCESSING", "SHIPPED"));

        assertEquals("order.status IN ('PENDING', 'PROCESSING', 'SHIPPED')", expression.toString());
    }

    @Test
    public void testInPermissionScenario() {
        // 权限检查场景
        InConditionExpression expression = new InConditionExpression("user", "role_id", Arrays.asList(1, 2, 5, 8));

        assertEquals("user.role_id IN (1, 2, 5, 8)", expression.toString());
    }

    // ========== 等价性测试 ==========

    @Test
    public void testEqualityBasedOnContent() {
        InConditionExpression expr1 = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE", "PENDING"));
        InConditionExpression expr2 = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE", "PENDING"));

        // 注意：这里测试toString等价性，因为InExpression没有重写equals方法
        assertEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInTableName() {
        InConditionExpression expr1 = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));
        InConditionExpression expr2 = new InConditionExpression("user", "status", Arrays.asList("ACTIVE"));

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInColumnName() {
        InConditionExpression expr1 = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));
        InConditionExpression expr2 = new InConditionExpression("tenant", "type", Arrays.asList("ACTIVE"));

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInValues() {
        InConditionExpression expr1 = new InConditionExpression("tenant", "status", Arrays.asList("ACTIVE"));
        InConditionExpression expr2 = new InConditionExpression("tenant", "status", Arrays.asList("PENDING"));

        assertNotEquals(expr1.toString(), expr2.toString());
    }
}