package io.github.anthem37.sql.rewriter.core.util;

import io.github.anthem37.sql.rewriter.core.extension.rule.AddColumnInsertRule;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionSelectRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionUpdateRule;
import io.github.anthem37.sql.rewriter.core.rule.IRule;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * RuleUtils 工具类单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class RuleUtilsTest {

    @Test
    public void testSortByPriorityWithNullList() {
        List<IRule> result = RuleUtils.sortByPriority(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSortByPriorityWithEmptyList() {
        List<IRule> result = RuleUtils.sortByPriority(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSortByPriorityWithSingleRule() {
        TestIRule rule = new TestIRule(5);
        List<IRule> input = Collections.singletonList(rule);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(1, result.size());
        assertEquals(rule, result.get(0));
    }

    @Test
    public void testSortByPriorityWithMultipleRules() {
        TestIRule rule1 = new TestIRule(10);
        TestIRule rule2 = new TestIRule(5);
        TestIRule rule3 = new TestIRule(15);
        List<IRule> input = Arrays.asList(rule1, rule2, rule3);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(3, result.size());
        assertEquals(rule2, result.get(0)); // Priority 5
        assertEquals(rule1, result.get(1)); // Priority 10
        assertEquals(rule3, result.get(2)); // Priority 15
    }

    @Test
    public void testSortByPriorityWithSamePriority() {
        TestIRule rule1 = new TestIRule(10);
        TestIRule rule2 = new TestIRule(10);
        TestIRule rule3 = new TestIRule(10);
        List<IRule> input = Arrays.asList(rule3, rule1, rule2);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(3, result.size());
        assertEquals(10, result.get(0).getPriority());
        assertEquals(10, result.get(1).getPriority());
        assertEquals(10, result.get(2).getPriority());
    }

    @Test
    public void testSortByPriorityDoesNotModifyOriginalList() {
        TestIRule rule1 = new TestIRule(10);
        TestIRule rule2 = new TestIRule(5);
        List<IRule> input = new ArrayList<>(Arrays.asList(rule1, rule2));
        List<IRule> original = new ArrayList<>(input);
        
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        // 原列表不变
        assertEquals(original.size(), input.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), input.get(i));
        }
        
        // 结果已排序
        assertEquals(rule2, result.get(0));
        assertEquals(rule1, result.get(1));
    }

    @Test
    public void testSortSqlRulesByPriorityWithNullList() {
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSortSqlRulesByPriorityWithEmptyList() {
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSortSqlRulesByPriorityWithSingleRule() {
        TestISqlRule rule = new TestISqlRule(5);
        List<ISqlRule<?>> input = Collections.singletonList(rule);
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(input);
        
        assertEquals(1, result.size());
        assertEquals(rule, result.get(0));
    }

    @Test
    public void testSortSqlRulesByPriorityWithMultipleRules() {
        TestISqlRule rule1 = new TestISqlRule(20);
        TestISqlRule rule2 = new TestISqlRule(10);
        TestISqlRule rule3 = new TestISqlRule(30);
        List<ISqlRule<?>> input = Arrays.asList(rule1, rule2, rule3);
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(input);
        
        assertEquals(3, result.size());
        assertEquals(rule2, result.get(0)); // Priority 10
        assertEquals(rule1, result.get(1)); // Priority 20
        assertEquals(rule3, result.get(2)); // Priority 30
    }

    @Test
    public void testSortSqlRulesByPriorityWithSamePriority() {
        TestISqlRule rule1 = new TestISqlRule(15);
        TestISqlRule rule2 = new TestISqlRule(15);
        TestISqlRule rule3 = new TestISqlRule(15);
        List<ISqlRule<?>> input = Arrays.asList(rule3, rule1, rule2);
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(input);
        
        assertEquals(3, result.size());
        assertEquals(15, result.get(0).getPriority());
        assertEquals(15, result.get(1).getPriority());
        assertEquals(15, result.get(2).getPriority());
    }

    @Test
    public void testSortSqlRulesByPriorityDoesNotModifyOriginalList() {
        TestISqlRule rule1 = new TestISqlRule(20);
        TestISqlRule rule2 = new TestISqlRule(10);
        List<ISqlRule<?>> input = new ArrayList<>(Arrays.asList(rule1, rule2));
        List<ISqlRule<?>> original = new ArrayList<>(input);
        
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(input);
        
        // 原列表不变
        assertEquals(original.size(), input.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), input.get(i));
        }
        
        // 结果已排序
        assertEquals(rule2, result.get(0));
        assertEquals(rule1, result.get(1));
    }

    @Test
    public void testSortByPriorityWithRealRules() {
        AddConditionSelectRule rule1 = new AddConditionSelectRule("user", new EqualToConditionExpression("user", "tenant_id", "TENANT_1"), 10);
        AddConditionSelectRule rule2 = new AddConditionSelectRule("user", new EqualToConditionExpression("user", "tenant_id", "TENANT_2"), 5);
        AddConditionSelectRule rule3 = new AddConditionSelectRule("user", new EqualToConditionExpression("user", "tenant_id", "TENANT_3"), 15);
        
        List<IRule> input = Arrays.asList(rule1, rule2, rule3);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getPriority());
        assertEquals(10, result.get(1).getPriority());
        assertEquals(15, result.get(2).getPriority());
    }

    @Test
    public void testSortSqlRulesByPriorityWithRealSqlRules() {
        AddColumnInsertRule rule1 = new AddColumnInsertRule("user", "tenant_id", "TENANT_1", 20);
        AddColumnInsertRule rule2 = new AddColumnInsertRule("user", "tenant_id", "TENANT_2", 10);
        AddColumnInsertRule rule3 = new AddColumnInsertRule("user", "tenant_id", "TENANT_3", 30);
        
        List<ISqlRule<?>> input = Arrays.asList(rule1, rule2, rule3);
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(input);
        
        assertEquals(3, result.size());
        assertEquals(10, result.get(0).getPriority());
        assertEquals(20, result.get(1).getPriority());
        assertEquals(30, result.get(2).getPriority());
    }

    @Test
    public void testSortByPriorityWithDefaultPriority() {
        TestIRule rule1 = new TestIRule(RulePriority.DEFAULT);
        TestIRule rule2 = new TestIRule(RulePriority.HIGH);
        List<IRule> input = Arrays.asList(rule1, rule2);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(2, result.size());
        assertEquals(rule2, result.get(0)); // HIGH priority first
        assertEquals(rule1, result.get(1)); // DEFAULT priority second
    }

    @Test
    public void testSortSqlRulesByPriorityWithDefaultPriority() {
        TestISqlRule rule1 = new TestISqlRule(RulePriority.DEFAULT);
        TestISqlRule rule2 = new TestISqlRule(RulePriority.HIGHEST);
        List<ISqlRule<?>> input = Arrays.asList(rule1, rule2);
        List<ISqlRule<?>> result = RuleUtils.sortSqlRulesByPriority(input);
        
        assertEquals(2, result.size());
        assertEquals(rule2, result.get(0)); // HIGHEST priority first
        assertEquals(rule1, result.get(1)); // DEFAULT priority second
    }

    @Test
    public void testSortByPriorityWithNegativePriority() {
        TestIRule rule1 = new TestIRule(-1);
        TestIRule rule2 = new TestIRule(1);
        List<IRule> input = Arrays.asList(rule1, rule2);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(2, result.size());
        assertEquals(rule1, result.get(0)); // -1 priority first
        assertEquals(rule2, result.get(1)); // 1 priority second
    }

    @Test
    public void testSortByPriorityWithZeroPriority() {
        TestIRule rule1 = new TestIRule(0);
        TestIRule rule2 = new TestIRule(1);
        List<IRule> input = Arrays.asList(rule1, rule2);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(2, result.size());
        assertEquals(rule1, result.get(0)); // 0 priority first
        assertEquals(rule2, result.get(1)); // 1 priority second
    }

    @Test
    public void testSortByPriorityWithLargePriority() {
        TestIRule rule1 = new TestIRule(Integer.MAX_VALUE);
        TestIRule rule2 = new TestIRule(Integer.MIN_VALUE);
        List<IRule> input = Arrays.asList(rule1, rule2);
        List<IRule> result = RuleUtils.sortByPriority(input);
        
        assertEquals(2, result.size());
        assertEquals(rule2, result.get(0)); // MIN_VALUE priority first
        assertEquals(rule1, result.get(1)); // MAX_VALUE priority second
    }

    /**
     * 测试用的 IRule 实现
     */
    private static class TestIRule implements IRule {
        private final int priority;

        public TestIRule(int priority) {
            this.priority = priority;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean match(Statement statement) {
            return false;
        }

        @Override
        public void apply(Statement statement) {
            // No implementation for test
        }
    }

    /**
     * 测试用的 ISqlRule 实现
     */
    private static class TestISqlRule implements ISqlRule<Statement> {
        private final int priority;

        public TestISqlRule(int priority) {
            this.priority = priority;
        }

        @Override
        public Class<Statement> getType() {
            return Statement.class;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public boolean match(Statement statement) {
            return false;
        }

        @Override
        public void apply(Statement statement) {
            // No implementation for test
        }

        @Override
        public void applyTyped(Statement statement) {
            // No implementation for test
        }
    }
}