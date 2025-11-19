package io.github.anthem37.sql.rewriter.core.extension.expression.impl;

import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * IsBooleanConditionExpression 单元测试
 */
public class IsBooleanConditionExpressionTest {

    // ========== IS TRUE 条件测试 ==========

    @Test
    public void testIsTrueCondition() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_active", false, true);

        assertEquals("user", expression.getTableName());
        assertEquals("is_active", expression.getColumnName());
        assertFalse(expression.getNot());
        assertTrue(expression.getIsTrue());
        assertEquals("user.is_active IS TRUE", expression.toString());
    }

    @Test
    public void testIsTrueConditionWithDifferentTableAndColumn() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("order", "is_paid", false, true);

        assertEquals("order", expression.getTableName());
        assertEquals("is_paid", expression.getColumnName());
        assertFalse(expression.getNot());
        assertTrue(expression.getIsTrue());
        assertEquals("order.is_paid IS TRUE", expression.toString());
    }

    // ========== IS FALSE 条件测试 ==========

    @Test
    public void testIsFalseCondition() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_deleted", false, false);

        assertEquals("user", expression.getTableName());
        assertEquals("is_deleted", expression.getColumnName());
        assertFalse(expression.getNot());
        assertFalse(expression.getIsTrue());
        assertEquals("user.is_deleted IS FALSE", expression.toString());
    }

    @Test
    public void testIsFalseConditionWithDifferentTableAndColumn() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("product", "is_available", false, false);

        assertEquals("product", expression.getTableName());
        assertEquals("is_available", expression.getColumnName());
        assertFalse(expression.getNot());
        assertFalse(expression.getIsTrue());
        assertEquals("product.is_available IS FALSE", expression.toString());
    }

    // ========== IS NOT TRUE 条件测试 ==========

    @Test
    public void testIsNotTrueCondition() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_active", true, true);

        assertEquals("user", expression.getTableName());
        assertEquals("is_active", expression.getColumnName());
        assertTrue(expression.getNot());
        assertTrue(expression.getIsTrue());
        assertEquals("user.is_active IS NOT TRUE", expression.toString());
    }

    @Test
    public void testIsNotTrueConditionWithDifferentTableAndColumn() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("task", "is_completed", true, true);

        assertEquals("task", expression.getTableName());
        assertEquals("is_completed", expression.getColumnName());
        assertTrue(expression.getNot());
        assertTrue(expression.getIsTrue());
        assertEquals("task.is_completed IS NOT TRUE", expression.toString());
    }

    // ========== IS NOT FALSE 条件测试 ==========

    @Test
    public void testIsNotFalseCondition() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_deleted", true, false);

        assertEquals("user", expression.getTableName());
        assertEquals("is_deleted", expression.getColumnName());
        assertTrue(expression.getNot());
        assertFalse(expression.getIsTrue());
        assertEquals("user.is_deleted IS NOT FALSE", expression.toString());
    }

    @Test
    public void testIsNotFalseConditionWithDifferentTableAndColumn() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("audit", "is_processed", true, false);

        assertEquals("audit", expression.getTableName());
        assertEquals("is_processed", expression.getColumnName());
        assertTrue(expression.getNot());
        assertFalse(expression.getIsTrue());
        assertEquals("audit.is_processed IS NOT FALSE", expression.toString());
    }

    // ========== 别名重构测试 ==========

    @Test
    public void testReconstructAliasExpressionIsTrue() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression("u");

        assertEquals("u", aliased.getTableName());
        assertEquals("is_active", aliased.getColumnName());
        assertFalse(aliased.getNot());
        assertTrue(aliased.getIsTrue());
        assertEquals("u.is_active IS TRUE", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionIsFalse() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_deleted", false, false);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression("u");

        assertEquals("u", aliased.getTableName());
        assertEquals("is_deleted", aliased.getColumnName());
        assertFalse(aliased.getNot());
        assertFalse(aliased.getIsTrue());
        assertEquals("u.is_deleted IS FALSE", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionIsNotTrue() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", true, true);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression("u");

        assertEquals("u", aliased.getTableName());
        assertEquals("is_active", aliased.getColumnName());
        assertTrue(aliased.getNot());
        assertTrue(aliased.getIsTrue());
        assertEquals("u.is_active IS NOT TRUE", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionIsNotFalse() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_deleted", true, false);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression("u");

        assertEquals("u", aliased.getTableName());
        assertEquals("is_deleted", aliased.getColumnName());
        assertTrue(aliased.getNot());
        assertFalse(aliased.getIsTrue());
        assertEquals("u.is_deleted IS NOT FALSE", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithComplexAlias() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression("user_tbl_123");

        assertEquals("user_tbl_123", aliased.getTableName());
        assertEquals("is_active", aliased.getColumnName());
        assertFalse(aliased.getNot());
        assertTrue(aliased.getIsTrue());
        assertEquals("user_tbl_123.is_active IS TRUE", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithEmptyAlias() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression("");

        assertEquals("", aliased.getTableName());
        assertEquals("is_active", aliased.getColumnName());
        assertFalse(aliased.getNot());
        assertTrue(aliased.getIsTrue());
        assertEquals("is_active IS TRUE", aliased.toString());
    }

    @Test
    public void testReconstructAliasExpressionWithNullAlias() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression aliased = (IsBooleanConditionExpression) original.reconstructAliasExpression(null);

        assertNull(aliased.getTableName());
        assertEquals("is_active", aliased.getColumnName());
        assertFalse(aliased.getNot());
        assertTrue(aliased.getIsTrue());
        assertEquals("is_active IS TRUE", aliased.toString());
    }

    // ========== 继承相关测试 ==========

    @Test
    public void testIsExpressionType() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_active", false, true);

        // 验证继承关系
        assertTrue(expression instanceof net.sf.jsqlparser.expression.Expression);
        assertTrue(expression instanceof net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression);
        assertTrue(expression instanceof IConditionExpression);
    }

    @Test
    public void testGetLeftExpression() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_active", false, true);

        assertNotNull(expression.getLeftExpression());
        assertEquals("user.is_active", expression.getLeftExpression().toString());
    }

    @Test
    public void testIsTrueFlag() {
        IsBooleanConditionExpression trueExpression = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression falseExpression = new IsBooleanConditionExpression("user", "is_active", false, false);

        assertTrue(trueExpression.isTrue());
        assertFalse(falseExpression.isTrue());
    }

    @Test
    public void testNotFlag() {
        IsBooleanConditionExpression notExpression = new IsBooleanConditionExpression("user", "is_active", true, true);
        IsBooleanConditionExpression normalExpression = new IsBooleanConditionExpression("user", "is_active", false, true);

        assertTrue(notExpression.isNot());
        assertFalse(normalExpression.isNot());
    }

    // ========== 边界条件测试 ==========

    @Test
    public void testWithSpecialCharactersInNames() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("table-name", "column_name", false, true);

        assertEquals("table-name", expression.getTableName());
        assertEquals("column_name", expression.getColumnName());
        assertEquals("table-name.column_name IS TRUE", expression.toString());
    }

    @Test
    public void testWithUnicodeCharacters() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("用户表", "是否激活", false, true);

        assertEquals("用户表", expression.getTableName());
        assertEquals("是否激活", expression.getColumnName());
        assertEquals("用户表.是否激活 IS TRUE", expression.toString());
    }

    @Test
    public void testWithNumericSuffixColumn() {
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("data", "is_active_1", false, true);

        assertEquals("data", expression.getTableName());
        assertEquals("is_active_1", expression.getColumnName());
        assertEquals("data.is_active_1 IS TRUE", expression.toString());
    }

    // ========== 实际使用场景测试 ==========

    @Test
    public void testUserActiveScenario() {
        // 用户激活状态检查
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_active", false, true);

        assertEquals("user.is_active IS TRUE", expression.toString());
    }

    @Test
    public void testSoftDeleteScenario() {
        // 软删除检查
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("user", "is_deleted", false, false);

        assertEquals("user.is_deleted IS FALSE", expression.toString());
    }

    @Test
    public void testTaskCompletionScenario() {
        // 任务完成检查
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("task", "is_completed", false, true);

        assertEquals("task.is_completed IS TRUE", expression.toString());
    }

    @Test
    public void testPermissionCheckScenario() {
        // 权限检查（未禁用）
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("permission", "is_disabled", true, true);

        assertEquals("permission.is_disabled IS NOT TRUE", expression.toString());
    }

    @Test
    public void testAvailabilityCheckScenario() {
        // 可用性检查（不是不可用）
        IsBooleanConditionExpression expression = new IsBooleanConditionExpression("product", "is_unavailable", true, false);

        assertEquals("product.is_unavailable IS NOT FALSE", expression.toString());
    }

    // ========== 复杂场景测试 ==========

    @Test
    public void testWithTableAliasInRealQuery() {
        // 模拟实际查询中的使用场景
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression withAlias = (IsBooleanConditionExpression) original.reconstructAliasExpression("u");

        String expectedQueryPart = "u.is_active IS TRUE";
        assertEquals(expectedQueryPart, withAlias.toString());

        // 验证可以在复杂查询中使用
        String fullQuery = "SELECT u.* FROM user u WHERE " + withAlias.toString();
        assertEquals("SELECT u.* FROM user u WHERE u.is_active IS TRUE", fullQuery);
    }

    @Test
    public void testMultipleBooleanConditions() {
        // 多个布尔条件组合的场景
        IsBooleanConditionExpression activeCheck = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression deletedCheck = new IsBooleanConditionExpression("user", "is_deleted", false, false);

        String combinedCondition = activeCheck.toString() + " AND " + deletedCheck.toString();
        assertEquals("user.is_active IS TRUE AND user.is_deleted IS FALSE", combinedCondition);
    }

    @Test
    public void testMixedBooleanConditions() {
        // 混合的布尔条件
        IsBooleanConditionExpression isTrue = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression isNotTrue = new IsBooleanConditionExpression("user", "is_blocked", true, true);
        IsBooleanConditionExpression isNotFalse = new IsBooleanConditionExpression("user", "is_verified", true, false);

        assertEquals("user.is_active IS TRUE", isTrue.toString());
        assertEquals("user.is_blocked IS NOT TRUE", isNotTrue.toString());
        assertEquals("user.is_verified IS NOT FALSE", isNotFalse.toString());
    }

    // ========== 等价性测试 ==========

    @Test
    public void testEqualityBasedOnContent() {
        IsBooleanConditionExpression expr1 = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression expr2 = new IsBooleanConditionExpression("user", "is_active", false, true);

        // 注意：这里测试toString等价性，因为IsBooleanExpression没有重写equals方法
        assertEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInTableName() {
        IsBooleanConditionExpression expr1 = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression expr2 = new IsBooleanConditionExpression("admin", "is_active", false, true);

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInColumnName() {
        IsBooleanConditionExpression expr1 = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression expr2 = new IsBooleanConditionExpression("user", "is_enabled", false, true);

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInNotFlag() {
        IsBooleanConditionExpression expr1 = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression expr2 = new IsBooleanConditionExpression("user", "is_active", true, true);

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    @Test
    public void testDifferenceInIsTrueFlag() {
        IsBooleanConditionExpression expr1 = new IsBooleanConditionExpression("user", "is_active", false, true);
        IsBooleanConditionExpression expr2 = new IsBooleanConditionExpression("user", "is_active", false, false);

        assertNotEquals(expr1.toString(), expr2.toString());
    }

    // ========== 接口实现测试 ==========

    @Test
    public void testReconstructAliasExpressionReturnsCorrectType() {
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);

        // 验证返回类型正确
        IConditionExpression result = original.reconstructAliasExpression("u");

        assertTrue(result instanceof IsBooleanConditionExpression);
        assertEquals("u.is_active IS TRUE", result.toString());
    }

    @Test
    public void testDefaultBehaviorOfReconstructAliasExpression() {
        // 测试接口的默认行为（当前类重写了该方法）
        IsBooleanConditionExpression original = new IsBooleanConditionExpression("user", "is_active", false, true);

        // 当前实现重写了reconstructAliasExpression，应该返回新实例
        IConditionExpression result = original.reconstructAliasExpression("u");

        assertNotSame(original, result); // 应该是不同的实例
        assertEquals("u.is_active IS TRUE", result.toString());
    }

    // ========== 所有组合测试 ==========

    @Test
    public void testAllCombinations() {
        // 测试所有可能的参数组合

        // IS TRUE
        IsBooleanConditionExpression isTrue = new IsBooleanConditionExpression("table", "col", false, true);
        assertEquals("table.col IS TRUE", isTrue.toString());

        // IS FALSE
        IsBooleanConditionExpression isFalse = new IsBooleanConditionExpression("table", "col", false, false);
        assertEquals("table.col IS FALSE", isFalse.toString());

        // IS NOT TRUE
        IsBooleanConditionExpression isNotTrue = new IsBooleanConditionExpression("table", "col", true, true);
        assertEquals("table.col IS NOT TRUE", isNotTrue.toString());

        // IS NOT FALSE
        IsBooleanConditionExpression isNotFalse = new IsBooleanConditionExpression("table", "col", true, false);
        assertEquals("table.col IS NOT FALSE", isNotFalse.toString());
    }

    @Test
    public void testConsistentBehaviorAcrossInstances() {
        // 验证不同实例的行为一致性
        for (int i = 0; i < 10; i++) {
            IsBooleanConditionExpression expression = new IsBooleanConditionExpression("table_" + i, "is_active_" + i, i % 2 == 0, i % 3 == 0);

            String expected = String.format("table_%d.is_active_%d IS %s%s",
                    i, i,
                    (i % 2 == 0) ? "NOT " : "",
                    (i % 3 == 0) ? "TRUE" : "FALSE");

            assertEquals(expected, expression.toString());
        }
    }
}