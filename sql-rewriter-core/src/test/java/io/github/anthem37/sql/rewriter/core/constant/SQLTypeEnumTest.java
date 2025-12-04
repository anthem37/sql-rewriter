package io.github.anthem37.sql.rewriter.core.constant;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SQLTypeEnum 单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class SQLTypeEnumTest {

    @Test
    public void testEnumValues() {
        SQLTypeEnum[] values = SQLTypeEnum.values();
        assertEquals(4, values.length);
    }

    @Test
    public void testValueOf() {
        assertEquals(SQLTypeEnum.INSERT, SQLTypeEnum.valueOf("INSERT"));
        assertEquals(SQLTypeEnum.DELETE, SQLTypeEnum.valueOf("DELETE"));
        assertEquals(SQLTypeEnum.UPDATE, SQLTypeEnum.valueOf("UPDATE"));
        assertEquals(SQLTypeEnum.SELECT, SQLTypeEnum.valueOf("SELECT"));
    }

    @Test
    public void testValueOfCase() {
        assertEquals(SQLTypeEnum.INSERT, SQLTypeEnum.valueOf("INSERT"));
        assertEquals(SQLTypeEnum.DELETE, SQLTypeEnum.valueOf("DELETE"));
        assertEquals(SQLTypeEnum.UPDATE, SQLTypeEnum.valueOf("UPDATE"));
        assertEquals(SQLTypeEnum.SELECT, SQLTypeEnum.valueOf("SELECT"));
    }

    @Test
    public void testEnumConstants() {
        assertNotNull(SQLTypeEnum.INSERT);
        assertNotNull(SQLTypeEnum.DELETE);
        assertNotNull(SQLTypeEnum.UPDATE);
        assertNotNull(SQLTypeEnum.SELECT);
    }

    @Test
    public void testEnumOrder() {
        SQLTypeEnum[] values = SQLTypeEnum.values();
        assertEquals(SQLTypeEnum.INSERT, values[0]);
        assertEquals(SQLTypeEnum.DELETE, values[1]);
        assertEquals(SQLTypeEnum.UPDATE, values[2]);
        assertEquals(SQLTypeEnum.SELECT, values[3]);
    }

    @Test
    public void testEnumToString() {
        assertEquals("INSERT", SQLTypeEnum.INSERT.toString());
        assertEquals("DELETE", SQLTypeEnum.DELETE.toString());
        assertEquals("UPDATE", SQLTypeEnum.UPDATE.toString());
        assertEquals("SELECT", SQLTypeEnum.SELECT.toString());
    }

    @Test
    public void testEnumName() {
        assertEquals("INSERT", SQLTypeEnum.INSERT.name());
        assertEquals("DELETE", SQLTypeEnum.DELETE.name());
        assertEquals("UPDATE", SQLTypeEnum.UPDATE.name());
        assertEquals("SELECT", SQLTypeEnum.SELECT.name());
    }

    @Test
    public void testEnumOrdinal() {
        assertEquals(0, SQLTypeEnum.INSERT.ordinal());
        assertEquals(1, SQLTypeEnum.DELETE.ordinal());
        assertEquals(2, SQLTypeEnum.UPDATE.ordinal());
        assertEquals(3, SQLTypeEnum.SELECT.ordinal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        SQLTypeEnum.valueOf("INVALID");
    }

    @Test
    public void testEnumEquality() {
        assertEquals(SQLTypeEnum.INSERT, SQLTypeEnum.INSERT);
        assertNotEquals(SQLTypeEnum.INSERT, SQLTypeEnum.DELETE);
        assertNotEquals(SQLTypeEnum.DELETE, SQLTypeEnum.UPDATE);
        assertNotEquals(SQLTypeEnum.UPDATE, SQLTypeEnum.SELECT);
    }

    @Test
    public void testEnumHashCode() {
        assertEquals(SQLTypeEnum.INSERT.hashCode(), SQLTypeEnum.INSERT.hashCode());
        assertNotEquals(SQLTypeEnum.INSERT.hashCode(), SQLTypeEnum.DELETE.hashCode());
    }

    @Test
    public void testEnumCompareTo() {
        assertTrue(SQLTypeEnum.INSERT.compareTo(SQLTypeEnum.DELETE) < 0);
        assertTrue(SQLTypeEnum.DELETE.compareTo(SQLTypeEnum.UPDATE) < 0);
        assertTrue(SQLTypeEnum.UPDATE.compareTo(SQLTypeEnum.SELECT) < 0);
        assertEquals(0, SQLTypeEnum.INSERT.compareTo(SQLTypeEnum.INSERT));
        assertTrue(SQLTypeEnum.SELECT.compareTo(SQLTypeEnum.INSERT) > 0);
    }
}