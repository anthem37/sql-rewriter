package io.github.anthem37.sql.rewiter.core.engine.impl;

import io.github.anthem37.sql.rewiter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewiter.core.extension.rule.AddColumnInsertRule;
import io.github.anthem37.sql.rewiter.core.extension.rule.AddConditionSelectRule;
import io.github.anthem37.sql.rewiter.core.rule.IRule;
import io.github.anthem37.sql.rewiter.core.rule.RulePriority;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SQLRewriteEngine 核心行为单元测试
 */
public class SQLRewriteEngineTest {

    @Test
    public void runShouldReturnOriginalSqlWhenRulesEmpty() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String originalSql = "SELECT * FROM tenant";

        String result = engine.run(originalSql);

        assertEquals(originalSql, result);
    }

    @Test
    public void runShouldApplyMatchingRule() {
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.singletonList(rule));

        String result = engine.run("SELECT * FROM tenant");

        assertEquals("SELECT * FROM tenant WHERE tenant.tenant_id = 'TENANT_1'", result);
    }

    @Test
    public void runShouldApplyAllMatchingRulesInPriorityOrder() {
        AddConditionSelectRule highPriorityRule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_3"), RulePriority.HIGH);
        AddConditionSelectRule mediumPriorityRule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_2"), RulePriority.MEDIUM);
        AddConditionSelectRule lowPriorityRule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"), RulePriority.LOW);
        SQLRewriteEngine engine = new SQLRewriteEngine(Arrays.asList(lowPriorityRule, mediumPriorityRule, highPriorityRule));

        String result = engine.run("SELECT * FROM tenant");

        // 验证所有条件都被正确添加，优先级顺序正确
        assertTrue(result.contains("tenant.tenant_id = 'TENANT_3'"));
        assertTrue(result.contains("tenant.tenant_id = 'TENANT_2'"));
        assertTrue(result.contains("tenant.tenant_id = 'TENANT_1'"));
        // 验证规则按优先级排序
        assertEquals(Arrays.asList(highPriorityRule, mediumPriorityRule, lowPriorityRule), new ArrayList<>(engine.getRules()));
    }

    @Test
    public void runShouldReturnOriginalSqlWhenRuleThrowsException() {
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.singletonList(rule));
        String originalSql = "INSERT INTO tenant SELECT 1";

        String result = engine.run(originalSql);

        assertEquals(originalSql, result);
    }

    @Test
    public void runShouldReturnOriginalSqlWhenParseFails() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String invalidSql = "INVALID SQL";

        String result = engine.run(invalidSql);

        assertEquals(invalidSql, result);
    }

    @Test
    public void constructorShouldSortRulesByPriority() {
        AddConditionSelectRule selectRule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"), RulePriority.HIGHEST);
        AddColumnInsertRule insertRule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1", RulePriority.LOWEST);
        SQLRewriteEngine engine = new SQLRewriteEngine(new ArrayList<>(Arrays.asList(insertRule, selectRule)));

        List<? extends IRule> sortedRules = engine.getRules();
        assertSame(selectRule, sortedRules.get(0));
        assertSame(insertRule, sortedRules.get(1));
    }

    // ========== 补充的测试场景 ==========

    @Test
    public void runWithNullSqlShouldReturnNull() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String result = engine.run(null);
        assertNull(result);
    }

    @Test
    public void runWithEmptySqlShouldReturnEmpty() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String result = engine.run("");
        assertEquals("", result);
    }

    @Test
    public void runWithWhitespaceOnlySqlShouldReturnOriginal() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());
        String originalSql = "   \n\t  ";
        String result = engine.run(originalSql);
        assertEquals(originalSql, result);
    }

    @Test
    public void runShouldNotModifyRulesList() {
        List<IRule> rules = new ArrayList<>();
        rules.add(new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1")));

        SQLRewriteEngine engine = new SQLRewriteEngine(rules);
        // 尝试修改原列表
        rules.clear();

        // 引擎的规则列表不应该受影响
        assertEquals(1, engine.getRules().size());
    }

    @Test
    public void runWithMixedRuleTypesShouldApplyCorrectly() {
        AddConditionSelectRule selectRule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
        AddColumnInsertRule insertRule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        SQLRewriteEngine engine = new SQLRewriteEngine(Arrays.asList(selectRule, insertRule));

        // 测试SELECT语句
        String selectResult = engine.run("SELECT * FROM tenant");
        assertEquals("SELECT * FROM tenant WHERE tenant.tenant_id = 'TENANT_1'", selectResult);

        // 测试INSERT语句（当前InsertRule只处理INSERT INTO table VALUES形式，不处理INSERT INTO SELECT）
        String insertResult = engine.run("INSERT INTO tenant (name) VALUES ('test')");
        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES ('test', 'TENANT_1')", insertResult);
    }

    @Test
    public void runWithMultipleTableSelectShouldApplyOnlyMatchingRules() {
        AddConditionSelectRule tenantRule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
        AddConditionSelectRule orderRule = new AddConditionSelectRule("orders", new EqualToConditionExpression("orders", "tenant_id", "TENANT_1"));

        SQLRewriteEngine engine = new SQLRewriteEngine(Arrays.asList(tenantRule, orderRule));

        String result = engine.run("SELECT * FROM tenant t JOIN orders o ON t.id = o.tenant_id");

        // 验证两个条件都被正确添加
        assertTrue(result.contains("t.tenant_id = 'TENANT_1'"));
        assertTrue(result.contains("o.tenant_id = 'TENANT_1'"));
    }

    @Test
    public void runWithComplexSqlShouldHandleGracefully() {
        AddConditionSelectRule rule = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.singletonList(rule));

        String complexSql = "WITH tenant_stats AS (SELECT tenant_id, COUNT(*) FROM orders GROUP BY tenant_id) " + "SELECT t.*, ts.order_count FROM tenant t LEFT JOIN tenant_stats ts ON t.id = ts.tenant_id " + "WHERE t.status = 'ACTIVE' ORDER BY t.created_at DESC";

        String result = engine.run(complexSql);

        // 验证条件被正确添加到主查询中
        assertTrue(result.contains("t.tenant_id = 'TENANT_1'"));
        assertTrue(result.contains("WITH tenant_stats"));
        assertTrue(result.contains("ORDER BY t.created_at DESC"));
    }

    @Test
    public void runWithSyntacticallyInvalidButParseableSqlShouldReturnOriginal() {
        SQLRewriteEngine engine = new SQLRewriteEngine(Collections.emptyList());

        // 语法上无效但JSQLParser能解析的SQL
        String invalidSql = "SELECT * FROM nonexistent_table WHERE invalid_column = 'value'";
        String result = engine.run(invalidSql);
        assertEquals(invalidSql, result);
    }

    @Test
    public void runShouldHandleLargeNumberOfRules() {
        List<IRule> rules = new ArrayList<>();
        // 创建100个规则
        for (int i = 0; i < 100; i++) {
            rules.add(new AddConditionSelectRule("table" + i, new EqualToConditionExpression("table" + i, "id", "value" + i)));
        }

        SQLRewriteEngine engine = new SQLRewriteEngine(rules);

        // 验证规则数量和排序
        assertEquals(100, engine.getRules().size());
        assertNotNull(engine.run("SELECT * FROM table0"));
    }

    @Test
    public void runWithDuplicateRulesShouldApplyAll() {
        AddConditionSelectRule rule1 = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_1"));
        AddConditionSelectRule rule2 = new AddConditionSelectRule("tenant", new EqualToConditionExpression("tenant", "tenant_id", "TENANT_2"));

        SQLRewriteEngine engine = new SQLRewriteEngine(Arrays.asList(rule1, rule2));

        String result = engine.run("SELECT * FROM tenant");

        // 验证两个条件都被添加
        assertTrue(result.contains("tenant.tenant_id = 'TENANT_1'"));
        assertTrue(result.contains("tenant.tenant_id = 'TENANT_2'"));
    }
}
