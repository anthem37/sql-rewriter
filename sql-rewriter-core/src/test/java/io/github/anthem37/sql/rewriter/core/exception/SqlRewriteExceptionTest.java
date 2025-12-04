package io.github.anthem37.sql.rewriter.core.exception;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SqlRewriteException 单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class SqlRewriteExceptionTest {

    @Test
    public void testConstructorWithErrorEnum() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);

        assertEquals("sql_blank", exception.getCode());
        assertEquals("SQL语句为空", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithErrorEnumAndArgs() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_PARSE_ERROR, "test error");

        assertEquals("sql_parse_error", exception.getCode());
        assertEquals("SQL解析异常: test error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithErrorEnumAndMultipleArgs() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_PARSE_ERROR, "test error", "line 10");

        assertEquals("sql_parse_error", exception.getCode());
        assertEquals("SQL解析异常: test error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithErrorEnumAndNullArgs() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_PARSE_ERROR, (Object) null);

        assertEquals("sql_parse_error", exception.getCode());
        assertEquals("SQL解析异常: null", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithCauseAndErrorEnum() {
        RuntimeException cause = new RuntimeException("Cause exception");
        SqlRewriteException exception = new SqlRewriteException(cause, ErrorEnum.SQL_REWRITE_ERROR, "rewrite failed");

        assertEquals("sql_rewrite_error", exception.getCode());
        assertEquals("SQL重写异常: rewrite failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithCauseAndErrorEnumAndNullArgs() {
        RuntimeException cause = new RuntimeException("Cause exception");
        SqlRewriteException exception = new SqlRewriteException(cause, ErrorEnum.SQL_REWRITE_ERROR, (Object) null);

        assertEquals("sql_rewrite_error", exception.getCode());
        assertEquals("SQL重写异常: null", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithCauseAndErrorEnumWithoutArgs() {
        RuntimeException cause = new RuntimeException("Cause exception");
        SqlRewriteException exception = new SqlRewriteException(cause, ErrorEnum.SQL_BLANK);

        assertEquals("sql_blank", exception.getCode());
        assertEquals("SQL语句为空", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithCodeAndMessage() {
        SqlRewriteException exception = new SqlRewriteException("custom_code", "Custom message");

        assertEquals("custom_code", exception.getCode());
        assertEquals("Custom message", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithCodeAndNullMessage() {
        SqlRewriteException exception = new SqlRewriteException("custom_code", null);

        assertEquals("custom_code", exception.getCode());
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithCodeAndMessageAndCause() {
        RuntimeException cause = new RuntimeException("Cause exception");
        SqlRewriteException exception = new SqlRewriteException("custom_code", "Custom message", cause);

        assertEquals("custom_code", exception.getCode());
        assertEquals("Custom message", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConstructorWithCodeAndNullMessageAndCause() {
        RuntimeException cause = new RuntimeException("Cause exception");
        SqlRewriteException exception = new SqlRewriteException("custom_code", null, cause);

        assertEquals("custom_code", exception.getCode());
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testIsRuntimeException() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testGetStackTrace() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    public void testSetStackTrace() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        StackTraceElement[] newStackTrace = new StackTraceElement[]{new StackTraceElement("TestClass", "testMethod", "TestClass.java", 10)};
        exception.setStackTrace(newStackTrace);

        assertArrayEquals(newStackTrace, exception.getStackTrace());
    }

    @Test
    public void testAddSuppressed() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        RuntimeException suppressed = new RuntimeException("Suppressed exception");
        exception.addSuppressed(suppressed);

        Throwable[] suppressedExceptions = exception.getSuppressed();
        assertEquals(1, suppressedExceptions.length);
        assertEquals(suppressed, suppressedExceptions[0]);
    }

    @Test
    public void testInitCause() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        RuntimeException cause = new RuntimeException("Cause exception");
        exception.initCause(cause);

        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testFillInStackTrace() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        Throwable result = exception.fillInStackTrace();

        assertEquals(exception, result);
    }

    @Test
    public void testGetLocalizedMessage() {
        SqlRewriteException exception = new SqlRewriteException(ErrorEnum.SQL_BLANK);
        assertEquals("SQL语句为空", exception.getLocalizedMessage());
    }

    @Test
    public void testToString() {
        SqlRewriteException exception = new SqlRewriteException("test_code", "Test message");
        String toString = exception.toString();
        assertTrue(toString.contains(SqlRewriteException.class.getName()));
        assertTrue(toString.contains("test_code"));
        assertTrue(toString.contains("Test message"));
    }

    @Test
    public void testExceptionChaining() {
        RuntimeException rootCause = new RuntimeException("Root cause");
        SqlRewriteException middleException = new SqlRewriteException(rootCause, ErrorEnum.SQL_PARSE_ERROR, "Middle error");
        SqlRewriteException topException = new SqlRewriteException(middleException, ErrorEnum.SQL_REWRITE_ERROR, "Top error");

        assertEquals("sql_rewrite_error", topException.getCode());
        assertTrue(topException.getMessage().contains("Top error"));
        assertEquals(middleException, topException.getCause());
        assertEquals(rootCause, middleException.getCause());
    }

    @Test
    public void testEqualsAndHashCode() {
        SqlRewriteException exception1 = new SqlRewriteException("test_code", "Test message");
        SqlRewriteException exception2 = new SqlRewriteException("test_code", "Test message");

        // 异常类的 equals 方法继承自 Object，默认比较引用
        assertNotEquals(exception1, exception2);
        assertNotEquals(exception1.hashCode(), exception2.hashCode());

        // 相同引用时相等
        SqlRewriteException exception3 = exception1;
        assertEquals(exception1, exception3);
        assertEquals(exception1.hashCode(), exception3.hashCode());
    }
}