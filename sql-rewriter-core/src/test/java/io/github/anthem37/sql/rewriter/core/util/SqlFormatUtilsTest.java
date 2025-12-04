package io.github.anthem37.sql.rewriter.core.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SqlFormatUtils 工具类单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class SqlFormatUtilsTest {

    @Test
    public void testCleanSqlWithNull() {
        String result = SqlFormatUtils.cleanSql(null);
        assertNull(result);
    }

    @Test
    public void testCleanSqlWithEmptyString() {
        String result = SqlFormatUtils.cleanSql("");
        assertEquals("", result);
    }

    @Test
    public void testCleanSqlWithWhitespaceOnly() {
        String result = SqlFormatUtils.cleanSql("   ");
        assertEquals("   ", result);
    }

    @Test
    public void testCleanSqlWithBlankString() {
        String result = SqlFormatUtils.cleanSql("   \n\t   ");
        assertEquals("   \n\t   ", result);
    }

    @Test
    public void testCleanSqlWithSimpleSelect() {
        String input = "SELECT * FROM user";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithLeadingWhitespace() {
        String input = "   SELECT * FROM user";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user", result);
    }

    @Test
    public void testCleanSqlWithTrailingWhitespace() {
        String input = "SELECT * FROM user   ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user", result);
    }

    @Test
    public void testCleanSqlWithBothLeadingAndTrailingWhitespace() {
        String input = "   SELECT * FROM user   ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user", result);
    }

    @Test
    public void testCleanSqlWithNewlines() {
        String input = "SELECT *\nFROM user\nWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithMultipleNewlines() {
        String input = "SELECT *\n\nFROM user\n\nWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithCarriageReturns() {
        String input = "SELECT *\rFROM user\rWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithWindowsNewlines() {
        String input = "SELECT *\r\nFROM user\r\nWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithTabs() {
        String input = "SELECT *\tFROM user\tWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithMultipleWhitespaceTypes() {
        String input = "SELECT *\t\n\rFROM user\t\n\rWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithMultipleSpaces() {
        String input = "SELECT  *  FROM  user  WHERE  id  =  1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithMixedWhitespace() {
        String input = "  SELECT  *\tFROM  user\nWHERE  id  =  1  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithComplexQuery() {
        String input = "  SELECT u.name,\t  o.amount\nFROM user u\r\nINNER JOIN order o ON u.id = o.user_id  \t\nWHERE  u.age  >  18  AND  o.status  =  'active'  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT u.name, o.amount FROM user u INNER JOIN order o ON u.id = o.user_id WHERE u.age > 18 AND o.status = 'active'", result);
    }

    @Test
    public void testCleanSqlWithSubquery() {
        String input = "SELECT * FROM user WHERE id IN (SELECT user_id FROM order WHERE amount > 100)";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithParentheses() {
        String input = "SELECT * FROM user WHERE (age > 18 AND status = 'active') OR (age < 65 AND status = 'premium')";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithQuotes() {
        String input = "SELECT * FROM user WHERE name = 'John Doe' AND address = \"123 Main St\"";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithNumbersAndDates() {
        String input = "SELECT * FROM orders WHERE amount > 100.50 AND order_date = '2023-01-01' AND count >= 10";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithInsertStatement() {
        String input = "  INSERT INTO user  (name,\t  age,\n  email)\r\nVALUES  ('John',\t  25,\n  'john@example.com')  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("INSERT INTO user (name, age, email) VALUES ('John', 25, 'john@example.com')", result);
    }

    @Test
    public void testCleanSqlWithUpdateStatement() {
        String input = "  UPDATE user\nSET  name  =  'John',\t  age  =  26\nWHERE  id  =  1  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("UPDATE user SET name = 'John', age = 26 WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithDeleteStatement() {
        String input = "  DELETE FROM user\nWHERE  age  <  18  AND  status  =  'inactive'  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("DELETE FROM user WHERE age < 18 AND status = 'inactive'", result);
    }

    @Test
    public void testCleanSqlWithFunctionCalls() {
        String input = "SELECT COUNT(*) as total, AVG(age) as avg_age FROM user WHERE status = 'active'";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithComments() {
        String input = "SELECT * FROM user -- This is a comment\nWHERE id = 1 /* Another comment */";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user -- This is a comment WHERE id = 1 /* Another comment */", result);
    }

    @Test
    public void testCleanSqlWithOnlyWhitespaceAfterCleaning() {
        String input = "  \t\n\r  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("  \t\n\r  ", result);
    }

    @Test
    public void testCleanSqlDoesNotChangeEmptyLinesToSpaces() {
        String input = "SELECT *\n\nFROM user\n\nWHERE id = 1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithUnicodeCharacters() {
        String input = "SELECT * FROM 用户 WHERE 姓名 = '张三' AND 年龄 > 18";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithBackticks() {
        String input = "SELECT `user`.`name`, `order`.`amount` FROM `user` JOIN `order` ON `user`.`id` = `order`.`user_id`";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithBrackets() {
        String input = "SELECT * FROM [user] WHERE [id] = 1 AND [name] LIKE '%test%'";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals(input, result);
    }

    @Test
    public void testCleanSqlWithLargeNumberSpaces() {
        String input = "SELECT    *    FROM    user    WHERE    id    =    1";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("SELECT * FROM user WHERE id = 1", result);
    }

    @Test
    public void testCleanSqlWithMixedCase() {
        String input = "  Select\t*\nFrom\tUser\nWhere\tId\t=\t1  ";
        String result = SqlFormatUtils.cleanSql(input);
        assertEquals("Select * From User Where Id = 1", result);
    }
}