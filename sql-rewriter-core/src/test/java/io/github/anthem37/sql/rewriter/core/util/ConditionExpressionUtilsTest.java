package io.github.anthem37.sql.rewriter.core.util;

import com.google.common.collect.Sets;
import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.InConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsBooleanConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsNullConditionExpression;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * ConditionExpressionUtils 工具类单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class ConditionExpressionUtilsTest {

    @Test
    public void testCreateAdaptiveConditionWithNullValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", null);

        assertNotNull(expression);
        assertTrue(expression instanceof IsNullConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithStringValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", "TENANT_001");

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithIntegerValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", 1001);

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithBooleanTrue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "active", true);

        assertNotNull(expression);
        assertTrue(expression instanceof IsBooleanConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithBooleanFalse() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "active", false);

        assertNotNull(expression);
        assertTrue(expression instanceof IsBooleanConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithArrayList() {
        List<String> tenantIds = Arrays.asList("TENANT_001", "TENANT_002", "TENANT_003");
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", tenantIds);

        assertNotNull(expression);
        assertTrue(expression instanceof InConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithSingletonList() {
        List<String> tenantIds = Collections.singletonList("TENANT_001");
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", tenantIds);

        assertNotNull(expression);
        assertTrue(expression instanceof InConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithEmptyList() {
        List<String> tenantIds = Collections.emptyList();
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", tenantIds);

        assertNotNull(expression);
        assertTrue(expression instanceof InConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithArrayAsObject() {
        // 测试数组类型，应该是Collection的子类
        Object[] array = {"TENANT_001", "TENANT_002"};
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", array);

        // 数组不是Collection，应该使用EqualToConditionExpression
        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithDoubleValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("product", "price", 99.99);

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithLongValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("order", "amount", 1000000L);

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithCharValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "status", 'A');

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithEnumValue() {
        TestEnum testEnum = TestEnum.ACTIVE;
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "status", testEnum);

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithComplexObject() {
        TestObject testObject = new TestObject("test");
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "data", testObject);

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithDifferentTableNames() {
        IConditionExpression expression1 = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", "TENANT_001");
        IConditionExpression expression2 = ConditionExpressionUtils.createAdaptiveCondition("order", "tenant_id", "TENANT_001");

        assertNotNull(expression1);
        assertNotNull(expression2);
        assertTrue(expression1 instanceof EqualToConditionExpression);
        assertTrue(expression2 instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithDifferentColumnNames() {
        IConditionExpression expression1 = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", "TENANT_001");
        IConditionExpression expression2 = ConditionExpressionUtils.createAdaptiveCondition("user", "org_id", "ORG_001");

        assertNotNull(expression1);
        assertNotNull(expression2);
        assertTrue(expression1 instanceof EqualToConditionExpression);
        assertTrue(expression2 instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithEmptyString() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "name", "");

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithZeroValue() {
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("product", "stock", 0);

        assertNotNull(expression);
        assertTrue(expression instanceof EqualToConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionWithNullTableOrColumn() {
        // 测试边界条件
        IConditionExpression expression1 = ConditionExpressionUtils.createAdaptiveCondition(null, "tenant_id", "TENANT_001");
        IConditionExpression expression2 = ConditionExpressionUtils.createAdaptiveCondition("user", null, "TENANT_001");

        assertNotNull(expression1);
        assertNotNull(expression2);
    }

    @Test
    public void testCreateAdaptiveConditionWithSet() {
        // Set也是Collection的子类
        Set<String> tenantIds = Sets.newHashSet();
        IConditionExpression expression = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", tenantIds);

        assertNotNull(expression);
        assertTrue(expression instanceof InConditionExpression);
    }

    @Test
    public void testCreateAdaptiveConditionTypeConsistency() {
        // 确保相同类型的输入总是产生相同类型的表达式
        IConditionExpression expression1 = ConditionExpressionUtils.createAdaptiveCondition("user", "tenant_id", "TENANT_001");
        IConditionExpression expression2 = ConditionExpressionUtils.createAdaptiveCondition("order", "tenant_id", "TENANT_001");

        assertEquals(expression1.getClass(), expression2.getClass());
    }

    /**
     * 测试用枚举
     */
    private enum TestEnum {
        ACTIVE, INACTIVE
    }

    /**
     * 测试用对象
     */
    private static class TestObject {
        private final String value;

        public TestObject(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}