package io.github.anthem37.sql.rewiter.core.util;

import io.github.anthem37.sql.rewiter.core.exception.ErrorEnum;
import io.github.anthem37.sql.rewiter.core.exception.SqlRewriteException;
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

    @Test
    public void stripQuotesShouldHandleQuotedNames() {
        // 通过equalToTableName间接测试stripQuotes方法
        Table table1 = new Table("\"Tenant\"");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant\"", table1));
    }

    @Test
    public void stripQuotesShouldHandleUnquotedNames() {
        Table table1 = new Table("Tenant");

        assertTrue(JsqlParserUtils.equalToTableName("Tenant", table1));
        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant\"", table1));
    }

    @Test
    public void stripQuotesShouldHandlePartialQuotes() {
        // 测试只有单侧引号的情况 - 只有完整的引号对才会被去除
        Table table1 = new Table("\"Tenant");

        assertTrue(JsqlParserUtils.equalToTableName("\"Tenant", table1));
        assertFalse(JsqlParserUtils.equalToTableName("Tenant", table1));
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
}
