package io.github.anthem37.sql.rewriter.core.util;

import io.github.anthem37.sql.rewriter.core.exception.ErrorEnum;
import io.github.anthem37.sql.rewriter.core.exception.SqlRewriteException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import static org.junit.Assert.*;

public class JsqlParserUtilsTest {

    @Test
    public void parseSqlShouldTrimAndParseValidSql() {
        Statement statement = JsqlParserUtils.parseSql("   SELECT * FROM tenant   \n\t");

        assertNotNull(statement);
        assertEquals("SELECT * FROM tenant", statement.toString());
    }

    @Test
    public void parseSqlShouldThrowExceptionWhenSqlIsBlank() {
        SqlRewriteException exception = assertThrows(SqlRewriteException.class, () -> JsqlParserUtils.parseSql("   "));

        assertEquals(ErrorEnum.SQL_BLANK.getErrorMsg(), exception.getMessage());
    }

    @Test
    public void parseSqlShouldThrowExceptionWhenParseFails() {
        SqlRewriteException exception = assertThrows(SqlRewriteException.class, () -> JsqlParserUtils.parseSql("INVALID"));

        assertTrue(exception.getMessage().contains(ErrorEnum.SQL_PARSE_ERROR.getErrorMsg().replace("{}", "INVALID")));
        assertEquals(ErrorEnum.SQL_PARSE_ERROR.getCode(), exception.getCode());
    }

    @Test
    public void createValueExpressionShouldHandleNull() {
        Expression expression = JsqlParserUtils.createValueExpression(null);

        assertTrue(expression instanceof NullValue);
    }

    @Test
    public void createValueExpressionShouldHandleLong() {
        Expression expression = JsqlParserUtils.createValueExpression(123L);

        assertTrue(expression instanceof LongValue);
        assertEquals("123", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleDate() {
        Date date = Date.valueOf("2025-11-13");

        Expression expression = JsqlParserUtils.createValueExpression(date);

        assertTrue(expression instanceof StringValue);
        assertEquals("'2025-11-13'", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleTime() {
        Time time = Time.valueOf("10:15:30");

        Expression expression = JsqlParserUtils.createValueExpression(time);

        assertEquals("'10:15:30'", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleTimestamp() {
        Timestamp timestamp = Timestamp.valueOf("2025-11-13 10:15:30");

        Expression expression = JsqlParserUtils.createValueExpression(timestamp);

        assertEquals("'2025-11-13 10:15:30'", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleString() {
        Expression expression = JsqlParserUtils.createValueExpression("TENANT");

        assertTrue(expression instanceof StringValue);
        assertEquals("'TENANT'", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleDouble() {
        Expression expression = JsqlParserUtils.createValueExpression(123.45);

        assertTrue(expression instanceof DoubleValue);
        assertEquals("123.45", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleHuToolDateTime() {
        cn.hutool.core.date.DateTime dateTime = new cn.hutool.core.date.DateTime("2025-11-13 15:30:45");

        Expression expression = JsqlParserUtils.createValueExpression(dateTime);

        assertTrue(expression instanceof StringValue);
        assertEquals("'2025-11-13 15:30:45'", expression.toString());
    }

    @Test
    public void equalToTableNameShouldMatchNameAndAliasCaseInsensitive() {
        Table table = new Table("Tenant");
        table.setAlias(new Alias("t"));

        assertTrue(JsqlParserUtils.equalToTableName("tenant", table));
        assertTrue(JsqlParserUtils.equalToTableName("t", table));
        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant\"", table));
    }

    @Test
    public void equalToTableNameShouldRespectAliasFlag() {
        Table table = new Table("Tenant");
        table.setAlias(new Alias("t"));

        assertTrue(JsqlParserUtils.equalToTableName("tenant", table, false));
        assertFalse(JsqlParserUtils.equalToTableName("t", table, false));
    }

    @Test
    public void getAliasShouldReturnAliasWhenPresent() {
        Table table = new Table("Tenant");
        table.setAlias(new Alias("t"));

        assertEquals("t", JsqlParserUtils.getAlias(table));
    }

    @Test
    public void getAliasShouldReturnTableNameWhenAliasMissing() {
        Table table = new Table("Tenant");

        assertEquals("Tenant", JsqlParserUtils.getAlias(table));
    }

    @Test
    public void createValueExpressionShouldHandleEmptyString() {
        Expression expression = JsqlParserUtils.createValueExpression("");

        assertTrue(expression instanceof StringValue);
        assertEquals("''", expression.toString());
    }

    @Test
    public void createValueExpressionShouldHandleSpecialCharacters() {
        Expression expression = JsqlParserUtils.createValueExpression("包含'单引号'和\"双引号\"的文本");

        assertTrue(expression instanceof StringValue);
        assertEquals("'包含'单引号'和\"双引号\"的文本'", expression.toString());
    }

    @Test
    public void getAliasShouldHandleNullTable() {
        Expression expression = JsqlParserUtils.createValueExpression(null);

        assertTrue(expression instanceof NullValue);
    }

    @Test
    public void getAliasShouldLogWarningForNullTable() {
        // 由于这是一个私有日志记录，我们主要测试不抛出异常
        String result = JsqlParserUtils.getAlias(null);

        assertNull(result);
    }

    // ========== stripQuotes 方法的全面测试 ==========

    @Test
    public void stripQuotesShouldHandleDoubleQuotes() {
        Table table1 = new Table("\"Tenant\"");
        Table table2 = new Table("\"my_table\"");
        Table table3 = new Table("\"123_table\"");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant\"", table1));

        assertTrue(JsqlParserUtils.equalToTableName("my_table", table2));
        assertTrue(JsqlParserUtils.equalToTableName("\"my_table\"", table2));

        assertTrue(JsqlParserUtils.equalToTableName("123_table", table3));
    }

    @Test
    public void stripQuotesShouldHandleSingleQuotes() {
        Table table1 = new Table("'Tenant'");
        Table table2 = new Table("'my_table'");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("'Tenant'", table1));

        assertTrue(JsqlParserUtils.equalToTableName("my_table", table2));
    }

    @Test
    public void stripQuotesShouldHandleBackticks() {
        Table table1 = new Table("`Tenant`");
        Table table2 = new Table("`my-table`");
        Table table3 = new Table("`123 table`");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("`Tenant`", table1));

        assertTrue(JsqlParserUtils.equalToTableName("my-table", table2));
        assertTrue(JsqlParserUtils.equalToTableName("123 table", table3));
    }

    @Test
    public void stripQuotesShouldHandleSquareBrackets() {
        Table table1 = new Table("[Tenant]");
        Table table2 = new Table("[my-table]");
        Table table3 = new Table("[123 table]");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("[Tenant]", table1));

        assertTrue(JsqlParserUtils.equalToTableName("my-table", table2));
        assertTrue(JsqlParserUtils.equalToTableName("123 table", table3));
    }

    @Test
    public void stripQuotesShouldHandleEscapedQuotes() {
        // 测试转义引号的处理
        Table table1 = new Table("\"My\"\"Table\"");  // "My""Table" -> My"Table
        Table table2 = new Table("'My''Table'");    // 'My''Table' -> My'Table
        Table table3 = new Table("`My``Table`");    // `My``Table` -> My`Table

        assertTrue(JsqlParserUtils.equalToTableName("My\"Table", table1));
        assertTrue(JsqlParserUtils.equalToTableName("My'Table", table2));
        assertTrue(JsqlParserUtils.equalToTableName("My`Table", table3));
    }

    @Test
    public void stripQuotesShouldHandleUnquotedNames() {
        Table table1 = new Table("Tenant");
        Table table2 = new Table("my_table");
        Table table3 = new Table("123_table");
        Table table4 = new Table("my-table");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant\"", table1));

        assertTrue(JsqlParserUtils.equalToTableName("my_table", table2));
        assertTrue(JsqlParserUtils.equalToTableName("`my_table`", table2));

        assertTrue(JsqlParserUtils.equalToTableName("123_table", table3));
        assertTrue(JsqlParserUtils.equalToTableName("[123_table]", table3));

        assertTrue(JsqlParserUtils.equalToTableName("my-table", table4));
        assertTrue(JsqlParserUtils.equalToTableName("`my-table`", table4));
    }

    @Test
    public void stripQuotesShouldHandlePartialQuotes() {
        // 测试只有单侧引号的情况 - 只有完整的引号对才会被去除
        Table table1 = new Table("\"Tenant");
        Table table2 = new Table("Tenant\"");
        Table table3 = new Table("'Tenant");
        Table table4 = new Table("Tenant'");
        Table table5 = new Table("`Tenant");
        Table table6 = new Table("Tenant`");
        Table table7 = new Table("[Tenant");
        Table table8 = new Table("Tenant]");

        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant", table1));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table1));

        assertTrue(JsqlParserUtils.equalToTableName("Tenant\"", table2));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table2));

        assertTrue(JsqlParserUtils.equalToTableName("'Tenant", table3));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table3));

        assertTrue(JsqlParserUtils.equalToTableName("Tenant'", table4));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table4));

        assertTrue(JsqlParserUtils.equalToTableName("`Tenant", table5));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table5));

        assertTrue(JsqlParserUtils.equalToTableName("Tenant`", table6));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table6));

        assertTrue(JsqlParserUtils.equalToTableName("[Tenant", table7));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table7));

        assertTrue(JsqlParserUtils.equalToTableName("Tenant]", table8));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table8));
    }

    @Test
    public void stripQuotesShouldHandleMixedQuoteTypes() {
        // 测试混合引号类型 - 不匹配的引号对不会被去除
        Table table1 = new Table("\"Tenant'");
        Table table2 = new Table("'Tenant\"");
        Table table3 = new Table("`Tenant\"");
        Table table4 = new Table("\"Tenant`");
        Table table5 = new Table("[Tenant\"");

        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant'", table1));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table1));

        assertTrue(JsqlParserUtils.equalToTableName("'Tenant\"", table2));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table2));

        assertTrue(JsqlParserUtils.equalToTableName("`Tenant\"", table3));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table3));

        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant`", table4));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table4));

        assertTrue(JsqlParserUtils.equalToTableName("[Tenant\"", table5));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table5));
    }

    @Test
    public void stripQuotesShouldHandleEdgeCases() {
        // 测试边界情况 - 专注于引号去除的核心功能

        Table table1 = new Table("\"");  // 单个双引号，不会去除引号
        assertTrue(JsqlParserUtils.equalToTableName("\"", table1));

        Table table2 = new Table("'");  // 单个单引号，不会去除引号
        assertTrue(JsqlParserUtils.equalToTableName("'", table2));

        Table table3 = new Table("`");  // 单个反引号，不会去除引号
        assertTrue(JsqlParserUtils.equalToTableName("`", table3));

        Table table4 = new Table("[");  // 单个左括号，不会去除引号
        assertTrue(JsqlParserUtils.equalToTableName("[", table4));

        Table table5 = new Table("]");  // 单个右括号，不会去除引号
        assertTrue(JsqlParserUtils.equalToTableName("]", table5));
    }

    @Test
    public void stripQuotesShouldHandleComplexRealWorldNames() {
        // 测试真实世界中复杂的表名
        Table table1 = new Table("\"user_info\"");  // 常规命名
        Table table2 = new Table("`order-items`");  // 带连字符
        Table table3 = new Table("[2023-sales]");   // 带数字和连字符
        Table table4 = new Table("'My \"Special\" Table'");  // 带转义引号
        Table table5 = new Table("`table with spaces`");  // 带空格
        Table table6 = new Table("[123 abc]");  // 数字开头，带空格

        assertTrue(JsqlParserUtils.equalToTableName("user_info", table1));
        assertTrue(JsqlParserUtils.equalToTableName("order-items", table2));
        assertTrue(JsqlParserUtils.equalToTableName("2023-sales", table3));
        assertTrue(JsqlParserUtils.equalToTableName("My \"Special\" Table", table4));
        assertTrue(JsqlParserUtils.equalToTableName("table with spaces", table5));
        assertTrue(JsqlParserUtils.equalToTableName("123 abc", table6));
    }

    @Test
    public void equalToTableNameShouldHandleNullInputs() {
        Table table = new Table("Tenant");

        assertFalse(JsqlParserUtils.equalToTableName(null, table));
        assertFalse(JsqlParserUtils.equalToTableName("", table));
        assertFalse(JsqlParserUtils.equalToTableName("tenant", null));
    }

    @Test
    public void equalToTableNameShouldHandleEmptyTableName() {
        Table table = new Table("");

        assertFalse(JsqlParserUtils.equalToTableName("tenant", table));
        assertFalse(JsqlParserUtils.equalToTableName("", table));
    }

    @Test
    public void equalToTableNameShouldHandleComplexAlias() {
        Table table = new Table("tenant");
        table.setAlias(new Alias("\"t_alias\""));

        assertTrue(JsqlParserUtils.equalToTableName("t_alias", table));
        assertTrue(JsqlParserUtils.equalToTableName("\"t_alias\"", table));
        assertFalse(JsqlParserUtils.equalToTableName("other_alias", table));
    }

    @Test
    public void equalToTableNameShouldHandleWhitespace() {
        Table table1 = new Table("  tenant  ");
        table1.setAlias(new Alias("  t  "));

        assertTrue(JsqlParserUtils.equalToTableName("tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("t", table1));

        Table table2 = new Table("  \"tenant\"  ");
        assertTrue(JsqlParserUtils.equalToTableName("tenant", table2));
    }

    @Test
    public void equalToTableNameShouldHandleComplexAliasWithQuotes() {
        // 测试复杂别名的引号处理
        Table table1 = new Table("tenant");
        table1.setAlias(new Alias("\"t_alias\""));
        assertTrue(JsqlParserUtils.equalToTableName("t_alias", table1));
        assertTrue(JsqlParserUtils.equalToTableName("\"t_alias\"", table1));

        Table table2 = new Table("tenant");
        table2.setAlias(new Alias("`t-alias`"));
        assertTrue(JsqlParserUtils.equalToTableName("t-alias", table2));
        assertTrue(JsqlParserUtils.equalToTableName("`t-alias`", table2));

        Table table3 = new Table("tenant");
        table3.setAlias(new Alias("[t-alias]"));
        assertTrue(JsqlParserUtils.equalToTableName("t-alias", table3));
        assertTrue(JsqlParserUtils.equalToTableName("[t-alias]", table3));

        Table table4 = new Table("tenant");
        table4.setAlias(new Alias("'t-alias'"));
        assertTrue(JsqlParserUtils.equalToTableName("t-alias", table4));
        assertTrue(JsqlParserUtils.equalToTableName("'t-alias'", table4));
    }

    @Test
    public void equalToTableNameShouldHandleSchemaQualifiedNames() {
        // 测试模式限定名称的处理 - 专注于stripQuotes本身的功能

        Table table1 = new Table("\"public\".\"tenant\"");
        assertTrue(JsqlParserUtils.equalToTableName("\"public\".\"tenant\"", table1));  // 直接匹配

        Table table2 = new Table("`public`.`tenant`");
        assertTrue(JsqlParserUtils.equalToTableName("`public`.`tenant`", table2));

        Table table3 = new Table("[public].[tenant]");
        assertTrue(JsqlParserUtils.equalToTableName("[public].[tenant]", table3));

        // 测试非引用的模式限定名称
        Table table4 = new Table("public.tenant");
        assertTrue(JsqlParserUtils.equalToTableName("public.tenant", table4));
    }

    @Test
    public void equalToTableNameShouldHandleMixedQuoteScenarios() {
        // 测试混合引号场景
        Table table1 = new Table("tenant");
        table1.setAlias(new Alias("\"t_alias\""));
        assertTrue(JsqlParserUtils.equalToTableName("t_alias", table1));

        Table table2 = new Table("\"tenant\"");
        table2.setAlias(new Alias("t_alias"));
        assertTrue(JsqlParserUtils.equalToTableName("tenant", table2));
        assertTrue(JsqlParserUtils.equalToTableName("t_alias", table2));

        Table table3 = new Table("`tenant`");
        table3.setAlias(new Alias("[t_alias]"));
        assertTrue(JsqlParserUtils.equalToTableName("tenant", table3));
        assertTrue(JsqlParserUtils.equalToTableName("t_alias", table3));
    }
}
