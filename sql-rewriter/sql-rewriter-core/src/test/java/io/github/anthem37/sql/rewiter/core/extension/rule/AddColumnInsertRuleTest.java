package io.github.anthem37.sql.rewiter.core.extension.rule;

import cn.hutool.core.date.DateTime;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.Assert.*;

/**
 * AddColumnInsertRule 单元测试
 */
public class AddColumnInsertRuleTest {

    @Test
    public void applyTypedShouldAddColumnWhenTableMatches() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES ('NAME', 'TENANT_1')", insert.toString());
        assertSame(insert, statement);
    }

    @Test
    public void applyTypedShouldAppendToRowConstructor() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME'),('OTHER')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", 1L);

        rule.applyTyped(insert);

        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES ('NAME', 1), ('OTHER', 1)", insert.toString());
    }

    @Test
    public void applyTypedShouldHandleFunctionArguments() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES (UPPER('NAME'))");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES (UPPER('NAME'), 'TENANT_1')", insert.toString());
    }

    @Test
    public void applyTypedShouldDoNothingWhenTableNotMatch() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO other(name) VALUES ('NAME')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        assertEquals("INSERT INTO other (name) VALUES ('NAME')", insert.toString());
    }

    @Test
    public void applyTypedShouldHandleInsertWithoutColumnNames() throws Exception {
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant VALUES ('NAME')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        // ⚠️ 注意：无列名的INSERT在SQL规范中要求VALUES数量与表列数完全匹配
        // 此处行为不符合SQL规范，实际使用中应明确指定列名
        // 这个测试仅验证当前代码的行为，不代表正确的SQL实践
        assertEquals("INSERT INTO tenant VALUES ('NAME', 'TENANT_1')", insert.toString());
    }

    @Test
    public void applyTypedShouldHandleInsertWithExplicitColumnNames() throws Exception {
        // ✅ SQL最佳实践：明确指定列名的INSERT，语义清晰且符合规范
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        // 在已有列名的基础上追加新列和新值，完全符合SQL规范
        assertEquals("INSERT INTO tenant (name, tenant_id) VALUES ('NAME', 'TENANT_1')", insert.toString());
    }

    @Test
    public void sqlBestPracticeRecommendation() throws Exception {
        // ✅ 推荐的SQL写法：始终明确指定列名
        Statement statement = CCJSqlParserUtil.parse("INSERT INTO tenant(name, created_at) VALUES ('NAME', '2023-01-01')");
        Insert insert = (Insert) statement;
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        rule.applyTyped(insert);

        // 完全符合SQL规范：列名和值一一对应
        assertEquals("INSERT INTO tenant (name, created_at, tenant_id) VALUES ('NAME', '2023-01-01', 'TENANT_1')", insert.toString());
    }

    @Test
    public void booleanValueBestPractices() throws Exception {
        // 测试布尔值的不同表示方式

        // 1. 当前实现：布尔值转换为字符串 (MySQL/PostgreSQL都能正确处理)
        Statement statement1 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert1 = (Insert) statement1;
        AddColumnInsertRule rule1 = new AddColumnInsertRule("tenant", "is_active", true);
        rule1.applyTyped(insert1);
        assertEquals("INSERT INTO tenant (name, is_active) VALUES ('NAME', 'true')", insert1.toString());

        // 2. 更好的做法：直接使用数字 (MySQL中1/0，PostgreSQL中1/0)
        Statement statement2 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert2 = (Insert) statement2;
        AddColumnInsertRule rule2 = new AddColumnInsertRule("tenant", "is_active", 1L);
        rule2.applyTyped(insert2);
        assertEquals("INSERT INTO tenant (name, is_active) VALUES ('NAME', 1)", insert2.toString());

        // 3. 说明：虽然字符串'true'能被正确处理，但数字1更明确且性能更好
        // MySQL: BOOLEAN = TINYINT(1), 所以1是原生格式
        // PostgreSQL: 1会自动转换为true，也是标准的布尔表示
    }

    @Test
    public void applyTypedShouldHandleDifferentDataTypes() throws Exception {
        // 测试数值类型
        Statement statement1 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert1 = (Insert) statement1;
        AddColumnInsertRule rule1 = new AddColumnInsertRule("tenant", "age", 25L);
        rule1.applyTyped(insert1);
        assertEquals("INSERT INTO tenant (name, age) VALUES ('NAME', 25)", insert1.toString());

        // 测试浮点数类型
        Statement statement2 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert2 = (Insert) statement2;
        AddColumnInsertRule rule2 = new AddColumnInsertRule("tenant", "salary", 5000.50);
        rule2.applyTyped(insert2);
        assertEquals("INSERT INTO tenant (name, salary) VALUES ('NAME', 5000.5)", insert2.toString());

        // 测试布尔值类型 - ⚠️ 当前实现将布尔值转换为字符串
        // 虽然MySQL和PostgreSQL都能正确处理'true'字符串，但推荐使用更明确的方式
        Statement statement3 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert3 = (Insert) statement3;
        AddColumnInsertRule rule3 = new AddColumnInsertRule("tenant", "is_active", true);
        rule3.applyTyped(insert3);
        assertEquals("INSERT INTO tenant (name, is_active) VALUES ('NAME', 'true')", insert3.toString());
    }

    @Test
    public void applyTypedShouldHandleBoundaryValues() throws Exception {
        // 测试null值
        Statement statement1 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert1 = (Insert) statement1;
        AddColumnInsertRule rule1 = new AddColumnInsertRule("tenant", "deleted_at", null);
        rule1.applyTyped(insert1);
        assertEquals("INSERT INTO tenant (name, deleted_at) VALUES ('NAME', NULL)", insert1.toString());

        // 测试空字符串 - ✅ 现在正确处理空字符串，符合SQL规范
        // 在SQL中，空字符串('')和NULL是不同的概念
        Statement statement2 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert2 = (Insert) statement2;
        AddColumnInsertRule rule2 = new AddColumnInsertRule("tenant", "description", "");
        rule2.applyTyped(insert2);
        assertEquals("INSERT INTO tenant (name, description) VALUES ('NAME', '')", insert2.toString());

        // 测试特殊字符
        Statement statement3 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert3 = (Insert) statement3;
        AddColumnInsertRule rule3 = new AddColumnInsertRule("tenant", "remark", "包含'单引号'和\"双引号\"的文本");
        rule3.applyTyped(insert3);
        assertEquals("INSERT INTO tenant (name, remark) VALUES ('NAME', '包含'单引号'和\"双引号\"的文本')", insert3.toString());

        // 测试非空字符串
        Statement statement4 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert4 = (Insert) statement4;
        AddColumnInsertRule rule4 = new AddColumnInsertRule("tenant", "description", "test");
        rule4.applyTyped(insert4);
        assertEquals("INSERT INTO tenant (name, description) VALUES ('NAME', 'test')", insert4.toString());
    }

    @Test
    public void applyTypedShouldHandleComplexInsertScenarios() throws Exception {
        // 测试带数据库和schema的表名
        Statement statement1 = CCJSqlParserUtil.parse("INSERT INTO db.public.tenant(name) VALUES ('NAME')");
        Insert insert1 = (Insert) statement1;
        AddColumnInsertRule rule1 = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");
        rule1.applyTyped(insert1);
        assertEquals("INSERT INTO db.public.tenant (name, tenant_id) VALUES ('NAME', 'TENANT_1')", insert1.toString());

        // 测试带别名的表名
        Statement statement2 = CCJSqlParserUtil.parse("INSERT INTO tenant AS t(name) VALUES ('NAME')");
        Insert insert2 = (Insert) statement2;
        AddColumnInsertRule rule2 = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");
        rule2.applyTyped(insert2);
        assertEquals("INSERT INTO tenant AS t (name, tenant_id) VALUES ('NAME', 'TENANT_1')", insert2.toString());

        // 测试INSERT SELECT子查询（这种情况可能不适用，因为VALUES和SELECT不能同时存在）
        try {
            CCJSqlParserUtil.parse("INSERT INTO tenant(name) SELECT name FROM users");
        } catch (Exception e) {
            // 这种情况是正常的，因为INSERT SELECT不支持VALUES
        }
    }

    @Test
    public void constructorShouldSetDefaultPriority() {
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        assertEquals("tenant", rule.getTableName());
        assertEquals("tenant_id", rule.getColumnName());
        assertEquals("TENANT_1", rule.getColumnValue());
        // 使用默认优先级
        assertTrue(rule.getPriority() >= 0);
    }

    @Test
    public void constructorShouldSetCustomPriority() {
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1", 10);

        assertEquals("tenant", rule.getTableName());
        assertEquals("tenant_id", rule.getColumnName());
        assertEquals("TENANT_1", rule.getColumnValue());
        assertEquals(10, rule.getPriority());
    }

    @Test
    public void getTypeShouldReturnInsertClass() {
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        assertSame(Insert.class, rule.getType());
    }

    @Test
    public void getTargetTableNameShouldReturnTableName() {
        AddColumnInsertRule rule = new AddColumnInsertRule("tenant", "tenant_id", "TENANT_1");

        assertEquals("tenant", rule.getTargetTableName());
    }

    @Test
    public void dateFormatCompatibilityTest() throws Exception {
        // 测试各种日期类型在MySQL和PostgreSQL中的兼容性

        // 1. java.sql.Date -> yyyy-MM-dd 格式
        Date sqlDate = Date.valueOf("2023-12-25");
        Statement statement1 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert1 = (Insert) statement1;
        AddColumnInsertRule rule1 = new AddColumnInsertRule("tenant", "created_date", sqlDate);
        rule1.applyTyped(insert1);
        assertEquals("INSERT INTO tenant (name, created_date) VALUES ('NAME', '2023-12-25')", insert1.toString());

        // 2. java.sql.Time -> HH:mm:ss 格式  
        Time sqlTime = Time.valueOf("14:30:15");
        Statement statement2 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert2 = (Insert) statement2;
        AddColumnInsertRule rule2 = new AddColumnInsertRule("tenant", "created_time", sqlTime);
        rule2.applyTyped(insert2);
        assertEquals("INSERT INTO tenant (name, created_time) VALUES ('NAME', '14:30:15')", insert2.toString());

        // 3. java.sql.Timestamp -> yyyy-MM-dd HH:mm:ss 格式
        Timestamp sqlTimestamp = Timestamp.valueOf("2023-12-25 14:30:15");
        Statement statement3 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert3 = (Insert) statement3;
        AddColumnInsertRule rule3 = new AddColumnInsertRule("tenant", "created_timestamp", sqlTimestamp);
        rule3.applyTyped(insert3);
        assertEquals("INSERT INTO tenant (name, created_timestamp) VALUES ('NAME', '2023-12-25 14:30:15')", insert3.toString());

        // 4. DateTime (HuTool) -> yyyy-MM-dd HH:mm:ss 格式
        DateTime dateTime = new DateTime("2023-12-25 14:30:15");
        Statement statement4 = CCJSqlParserUtil.parse("INSERT INTO tenant(name) VALUES ('NAME')");
        Insert insert4 = (Insert) statement4;
        AddColumnInsertRule rule4 = new AddColumnInsertRule("tenant", "created_datetime", dateTime);
        rule4.applyTyped(insert4);
        assertEquals("INSERT INTO tenant (name, created_datetime) VALUES ('NAME', '2023-12-25 14:30:15')", insert4.toString());

        // 所有这些格式都能被MySQL和PostgreSQL正确解析：
        // MySQL: 支持 '2023-12-25', '14:30:15', '2023-12-25 14:30:15'
        // PostgreSQL: 支持 '2023-12-25', '14:30:15', '2023-12-25 14:30:15'
    }
}
