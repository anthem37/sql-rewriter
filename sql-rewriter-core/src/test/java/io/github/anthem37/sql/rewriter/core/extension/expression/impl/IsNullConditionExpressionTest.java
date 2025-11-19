package io.github.anthem37.sql.rewriter.core.extension.expression.impl;

import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * IsNullConditionExpression 单元测试
 */
public class IsNullConditionExpressionTest {

    // ========== 基础功能测试 ==========

    @Test
    public void testConstructor() {
        IsNullConditionExpression expression = new IsNullConditionExpression("tenant", "deleted_at");

        assertEquals("tenant", expression.getTableName());
        assertEquals("deleted_at", expression.getColumnName());
        assertEquals("tenant.deleted_at IS NULL", expression.toString());
    }

    @Test
    public void testConstructorWithDifferentTableAndColumn() {
        IsNullConditionExpression expression = new IsNullConditionExpression("user", "last_login");

        assertEquals("user", expression.getTableName());
        assertEquals("last_login", expression.getColumnName());
        assertEquals("user.last_login IS NULL", expression.toString());
    }

    // ========== 别名重构测试 ==========

    @Test
    public void testReconstructAliasExpressionWithSimpleAlias() {
        IsNullConditionExpression original = new IsNullConditionExpression("tenant", "deleted_at");
        IsNullConditionExpression aliased = (IsNullConditionExpression) original.reconstructAliasExpression("t");

        assertEquals("t", aliased.getTableName());
        assertEquals("deleted_at", aliased.getColumnName());
        assertEquals("t.deleted_at IS NULL", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithComplexAlias() {
        IsNullConditionExpression original = new IsNullConditionExpression("user", "last_login");
        IsNullConditionExpression aliased = (IsNullConditionExpression) original.reconstructAliasExpression("u_123");

        assertEquals("u_123", aliased.getTableName());
        assertEquals("last_login", aliased.getColumnName());
        assertEquals("u_123.last_login IS NULL", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithEmptyAlias() {
        IsNullConditionExpression original = new IsNullConditionExpression("tenant", "deleted_at");
        IsNullConditionExpression aliased = (IsNullConditionExpression) original.reconstructAliasExpression("");

        assertEquals("", aliased.getTableName());
        assertEquals("deleted_at", aliased.getColumnName());
        assertEquals("deleted_at IS NULL", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithNullAlias() {
        IsNullConditionExpression original = new IsNullConditionExpression("tenant", "deleted_at");
        IsNullConditionExpression aliased = (IsNullConditionExpression) original.reconstructAliasExpression(null);

        assertNull(aliased.getTableName());
        assertEquals("deleted_at", aliased.getColumnName());
        assertEquals("deleted_at IS NULL", aliased.toString());
    }

    // ========== 继承相关测试 ==========

    @Test
    public void testIsExpressionType() {
        IsNullConditionExpression expression = new IsNullConditionExpression("tenant", "deleted_at");

        // 验证继承关系
        assertTrue(expression instanceof net.sf.jsqlparser.expression.Expression);
        assertTrue(expression instanceof net.sf.jsqlparser.expression.operators.relational.IsNullExpression);
        assertTrue(expression instanceof IConditionExpression);
    }

    @Test
    public void testGetLeftExpression() {
        IsNullConditionExpression expression = new IsNullConditionExpression("tenant", "deleted_at");

        assertNotNull(expression.getLeftExpression());
        assertEquals("tenant.deleted_at", expression.getLeftExpression().toString());
    }

    @Test
    public void testIsNotNullProperty() {
        IsNullConditionExpression expression = new IsNullConditionExpression("tenant", "deleted_at");

        // 默认情况下是IS NULL，不是IS NOT NULL
        assertFalse(expression.isNot());
    }

    // ========== 边界条件测试 ==========

    @Test
    public void testWithEmptyTableName() {
        IsNullConditionExpression expression = new IsNullConditionExpression("", "column");

        assertEquals("", expression.getTableName());
        assertEquals("column", expression.getColumnName());
        assertEquals("column IS NULL", expression.toString());
    }

    @Test
    public void testWithEmptyColumnName() {
        IsNullConditionExpression expression = new IsNullConditionExpression("tenant", "");

        assertEquals("tenant", expression.getTableName());
        assertEquals("", expression.getColumnName());
        assertEquals("tenant. IS NULL", expression.toString());
    }

    @Test
    public void testWithSpecialCharactersInNames() {
        IsNullConditionExpression expression = new IsNullConditionExpression("table-name", "column_name");

        assertEquals("table-name.column_name IS NULL", expression.toString());
    }

    @Test
    public void testWithUnicodeCharacters() {
        IsNullConditionExpression expression = new IsNullConditionExpression("用户表", "删除时间");

        assertEquals("用户表.删除时间 IS NULL", expression.toString());
    }

    // ========== 实际使用场景测试 ==========

    @Test
    public void testSoftDeleteScenario() {
        // 软删除场景
        IsNullConditionExpression expression = new IsNullConditionExpression("user", "deleted_at");

        assertEquals("user.deleted_at IS NULL", expression.toString());
    }

    @Test
    public void testOptionalFieldScenario() {
        // 可选字段场景
        IsNullConditionExpression expression = new IsNullConditionExpression("profile", "bio");

        assertEquals("profile.bio IS NULL", expression.toString());
    }

    @Test
    public void testAuditScenario() {
        // 审计字段场景
        IsNullConditionExpression expression = new IsNullConditionExpression("order", "approved_at");

        assertEquals("order.approved_at IS NULL", expression.toString());
    }

    @Test
    public void testRelationScenario() {
        // 关联关系场景
        IsNullConditionExpression expression = new IsNullConditionExpression("comment", "parent_id");

        assertEquals("comment.parent_id IS NULL", expression.toString());
    }

    // ========== 等价性测试 ==========

    @Test
    public void testEqualityBasedOnContent() {
        IsNullConditionExpression expr1 = new IsNullConditionExpression("tenant", "deleted_at");
        IsNullConditionExpression expr2 = new IsNullConditionExpression("tenant", "deleted_at");

        // 注意：这里测试toString等价性，因为IsNullExpression没有重写equals方法
        assertEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInTableName() {
        IsNullConditionExpression expr1 = new IsNullConditionExpression("tenant", "deleted_at");
        IsNullConditionExpression expr2 = new IsNullConditionExpression("user", "deleted_at");

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInColumnName() {
        IsNullConditionExpression expr1 = new IsNullConditionExpression("tenant", "deleted_at");
        IsNullConditionExpression expr2 = new IsNullConditionExpression("tenant", "archived_at");

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    // ========== 复杂场景测试 ==========

    @Test
    public void testWithTableAliasInRealQuery() {
        // 模拟实际查询中的使用场景
        IsNullConditionExpression original = new IsNullConditionExpression("user", "deleted_at");
        IsNullConditionExpression withAlias = (IsNullConditionExpression) original.reconstructAliasExpression("u");

        String expectedQueryPart = "u.deleted_at IS NULL";
        assertEquals(expectedQueryPart, withAlias.toString());

        // 验证可以在复杂查询中使用
        String fullQuery = "SELECT u.* FROM user u WHERE " + withAlias.toString();
        assertEquals("SELECT u.* FROM user u WHERE u.deleted_at IS NULL", fullQuery);
    }

    @Test
    public void testWithMultipleNullChecks() {
        // 多个NULL检查的场景
        IsNullConditionExpression deletedCheck = new IsNullConditionExpression("user", "deleted_at");
        IsNullConditionExpression archivedCheck = new IsNullConditionExpression("user", "archived_at");

        String combinedCondition = deletedCheck.toString() + " AND " + archivedCheck.toString();
        assertEquals("user.deleted_at IS NULL AND user.archived_at IS NULL", combinedCondition);
    }

    @Test
    public void testConsistentBehaviorAcrossInstances() {
        // 验证不同实例的行为一致性
        for (int i = 0; i < 10; i++) {
            IsNullConditionExpression expression = new IsNullConditionExpression("table_" + i, "column_" + i);
            assertEquals("table_" + i + ".column_" + i + " IS NULL", expression.toString());
        }
    }

    // ========== 接口实现测试 ==========

    @Test
    public void testReconstructAliasExpressionReturnsCorrectType() {
        IsNullConditionExpression original = new IsNullConditionExpression("tenant", "deleted_at");

        // 验证返回类型正确
        IConditionExpression result = original.reconstructAliasExpression("t");

        assertTrue(result instanceof IsNullConditionExpression);
        assertEquals("t.deleted_at IS NULL", result.toString());
    }

    @Test
    public void testDefaultBehaviorOfReconstructAliasExpression() {
        // 测试接口的默认行为（当前类重写了该方法）
        IsNullConditionExpression original = new IsNullConditionExpression("tenant", "deleted_at");

        // 当前实现重写了reconstructAliasExpression，应该返回新实例
        IConditionExpression result = original.reconstructAliasExpression("t");

        assertNotSame(original, result); // 应该是不同的实例
        assertEquals("t.deleted_at IS NULL", result.toString());
    }
}