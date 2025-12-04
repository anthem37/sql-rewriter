package io.github.anthem37.sql.rewriter.core.exception;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ErrorEnum 单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class ErrorEnumTest {

    @Test
    public void testEnumValues() {
        ErrorEnum[] values = ErrorEnum.values();
        assertEquals(3, values.length);
    }

    @Test
    public void testValueOf() {
        assertEquals(ErrorEnum.SQL_BLANK, ErrorEnum.valueOf("SQL_BLANK"));
        assertEquals(ErrorEnum.SQL_PARSE_ERROR, ErrorEnum.valueOf("SQL_PARSE_ERROR"));
        assertEquals(ErrorEnum.SQL_REWRITE_ERROR, ErrorEnum.valueOf("SQL_REWRITE_ERROR"));
    }

    @Test
    public void testGetCode() {
        assertEquals("sql_blank", ErrorEnum.SQL_BLANK.getCode());
        assertEquals("sql_parse_error", ErrorEnum.SQL_PARSE_ERROR.getCode());
        assertEquals("sql_rewrite_error", ErrorEnum.SQL_REWRITE_ERROR.getCode());
    }

    @Test
    public void testGetErrorMsg() {
        assertEquals("SQL语句为空", ErrorEnum.SQL_BLANK.getErrorMsg());
        assertEquals("SQL解析异常: {}", ErrorEnum.SQL_PARSE_ERROR.getErrorMsg());
        assertEquals("SQL重写异常: {}", ErrorEnum.SQL_REWRITE_ERROR.getErrorMsg());
    }

    @Test
    public void testFormatMsgWithoutArgs() {
        assertEquals("SQL语句为空", ErrorEnum.SQL_BLANK.formatMsg());
    }

    @Test
    public void testFormatMsgWithArgs() {
        assertEquals("SQL解析异常: test error", ErrorEnum.SQL_PARSE_ERROR.formatMsg("test error"));
        assertEquals("SQL重写异常: rewrite failed", ErrorEnum.SQL_REWRITE_ERROR.formatMsg("rewrite failed"));
    }

    @Test
    public void testFormatMsgWithMultipleArgs() {
        assertEquals("SQL解析异常: test error - line 10", ErrorEnum.SQL_PARSE_ERROR.formatMsg("test error - line 10"));
        assertEquals("SQL重写异常: test error - line 10", ErrorEnum.SQL_REWRITE_ERROR.formatMsg("test error - line 10"));
    }

    @Test
    public void testFormatMsgWithNullArg() {
        assertEquals("SQL解析异常: null", ErrorEnum.SQL_PARSE_ERROR.formatMsg((Object) null));
        assertEquals("SQL重写异常: null", ErrorEnum.SQL_REWRITE_ERROR.formatMsg((Object) null));
    }

    @Test
    public void testFormatMsgWithEmptyArgs() {
        assertEquals("SQL解析异常: ", ErrorEnum.SQL_PARSE_ERROR.formatMsg(""));
        assertEquals("SQL重写异常: ", ErrorEnum.SQL_REWRITE_ERROR.formatMsg(""));
    }

    @Test
    public void testEnumConstants() {
        assertNotNull(ErrorEnum.SQL_BLANK);
        assertNotNull(ErrorEnum.SQL_PARSE_ERROR);
        assertNotNull(ErrorEnum.SQL_REWRITE_ERROR);
    }

    @Test
    public void testEnumOrder() {
        ErrorEnum[] values = ErrorEnum.values();
        assertEquals(ErrorEnum.SQL_BLANK, values[0]);
        assertEquals(ErrorEnum.SQL_PARSE_ERROR, values[1]);
        assertEquals(ErrorEnum.SQL_REWRITE_ERROR, values[2]);
    }

    @Test
    public void testEnumToString() {
        assertEquals("SQL_BLANK", ErrorEnum.SQL_BLANK.toString());
        assertEquals("SQL_PARSE_ERROR", ErrorEnum.SQL_PARSE_ERROR.toString());
        assertEquals("SQL_REWRITE_ERROR", ErrorEnum.SQL_REWRITE_ERROR.toString());
    }

    @Test
    public void testEnumName() {
        assertEquals("SQL_BLANK", ErrorEnum.SQL_BLANK.name());
        assertEquals("SQL_PARSE_ERROR", ErrorEnum.SQL_PARSE_ERROR.name());
        assertEquals("SQL_REWRITE_ERROR", ErrorEnum.SQL_REWRITE_ERROR.name());
    }

    @Test
    public void testEnumOrdinal() {
        assertEquals(0, ErrorEnum.SQL_BLANK.ordinal());
        assertEquals(1, ErrorEnum.SQL_PARSE_ERROR.ordinal());
        assertEquals(2, ErrorEnum.SQL_REWRITE_ERROR.ordinal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        ErrorEnum.valueOf("INVALID");
    }

    @Test
    public void testEnumEquality() {
        assertEquals(ErrorEnum.SQL_BLANK, ErrorEnum.SQL_BLANK);
        assertNotEquals(ErrorEnum.SQL_BLANK, ErrorEnum.SQL_PARSE_ERROR);
        assertNotEquals(ErrorEnum.SQL_PARSE_ERROR, ErrorEnum.SQL_REWRITE_ERROR);
    }

    @Test
    public void testEnumHashCode() {
        assertEquals(ErrorEnum.SQL_BLANK.hashCode(), ErrorEnum.SQL_BLANK.hashCode());
        assertNotEquals(ErrorEnum.SQL_BLANK.hashCode(), ErrorEnum.SQL_PARSE_ERROR.hashCode());
    }

    @Test
    public void testEnumCompareTo() {
        assertTrue(ErrorEnum.SQL_BLANK.compareTo(ErrorEnum.SQL_PARSE_ERROR) < 0);
        assertTrue(ErrorEnum.SQL_PARSE_ERROR.compareTo(ErrorEnum.SQL_REWRITE_ERROR) < 0);
        assertEquals(0, ErrorEnum.SQL_BLANK.compareTo(ErrorEnum.SQL_BLANK));
        assertTrue(ErrorEnum.SQL_REWRITE_ERROR.compareTo(ErrorEnum.SQL_BLANK) > 0);
    }

    @Test
    public void testFormatMsgWithComplexArgs() {
        String complexArg = "Error at line 10, column 5: unexpected token";
        String result = ErrorEnum.SQL_PARSE_ERROR.formatMsg(complexArg);
        assertEquals("SQL解析异常: Error at line 10, column 5: unexpected token", result);
    }

    @Test
    public void testFormatMsgWithNumericArgs() {
        assertEquals("SQL解析异常: 12345", ErrorEnum.SQL_PARSE_ERROR.formatMsg(12345));
        assertEquals("SQL重写异常: 99.99", ErrorEnum.SQL_REWRITE_ERROR.formatMsg(99.99));
    }

    @Test
    public void testFormatMsgWithBooleanArgs() {
        assertEquals("SQL解析异常: true", ErrorEnum.SQL_PARSE_ERROR.formatMsg(true));
        assertEquals("SQL重写异常: false", ErrorEnum.SQL_REWRITE_ERROR.formatMsg(false));
    }
}