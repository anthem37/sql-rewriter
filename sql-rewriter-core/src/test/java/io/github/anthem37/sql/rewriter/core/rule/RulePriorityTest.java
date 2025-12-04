package io.github.anthem37.sql.rewriter.core.rule;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

/**
 * RulePriority 工具类单元测试
 *
 * @author anthem37
 * @since 2025/11/20
 */
public class RulePriorityTest {

    @Test
    public void testHighestPriority() {
        assertEquals(1, RulePriority.HIGHEST);
    }

    @Test
    public void testHighPriority() {
        assertEquals(5, RulePriority.HIGH);
    }

    @Test
    public void testMediumPriority() {
        assertEquals(10, RulePriority.MEDIUM);
    }

    @Test
    public void testLowPriority() {
        assertEquals(20, RulePriority.LOW);
    }

    @Test
    public void testLowestPriority() {
        assertEquals(50, RulePriority.LOWEST);
    }

    @Test
    public void testDefaultPriority() {
        assertEquals(100, RulePriority.DEFAULT);
    }

    @Test
    public void testSelectDefaultPriority() {
        assertEquals(10, RulePriority.SELECT_DEFAULT);
    }

    @Test
    public void testInsertDefaultPriority() {
        assertEquals(20, RulePriority.INSERT_DEFAULT);
    }

    @Test
    public void testUpdateDefaultPriority() {
        assertEquals(30, RulePriority.UPDATE_DEFAULT);
    }

    @Test
    public void testDeleteDefaultPriority() {
        assertEquals(40, RulePriority.DELETE_DEFAULT);
    }

    @Test
    public void testPriorityOrder() {
        assertTrue(RulePriority.HIGHEST < RulePriority.HIGH);
        assertTrue(RulePriority.HIGH < RulePriority.MEDIUM);
        assertTrue(RulePriority.MEDIUM < RulePriority.LOW);
        assertTrue(RulePriority.LOW < RulePriority.LOWEST);
        assertTrue(RulePriority.LOWEST < RulePriority.DEFAULT);
    }

    @Test
    public void testSqlTypeDefaultPriorityOrder() {
        assertTrue(RulePriority.SELECT_DEFAULT < RulePriority.INSERT_DEFAULT);
        assertTrue(RulePriority.INSERT_DEFAULT < RulePriority.UPDATE_DEFAULT);
        assertTrue(RulePriority.UPDATE_DEFAULT < RulePriority.DELETE_DEFAULT);
    }

    @Test
    public void testPriorityValues() {
        assertEquals(1, RulePriority.HIGHEST);
        assertEquals(5, RulePriority.HIGH);
        assertEquals(10, RulePriority.MEDIUM);
        assertEquals(10, RulePriority.SELECT_DEFAULT);
        assertEquals(20, RulePriority.INSERT_DEFAULT);
        assertEquals(30, RulePriority.UPDATE_DEFAULT);
        assertEquals(40, RulePriority.DELETE_DEFAULT);
        assertEquals(50, RulePriority.LOWEST);
        assertEquals(100, RulePriority.DEFAULT);
    }

    @Test
    public void testSelectDefaultEqualsMedium() {
        assertEquals(RulePriority.MEDIUM, RulePriority.SELECT_DEFAULT);
    }

    @Test
    public void testConstructorPrivate() throws NoSuchMethodException {
        Constructor<RulePriority> constructor = RulePriority.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        assertTrue(constructor.getModifiers() == java.lang.reflect.Modifier.PRIVATE);
    }

    @Test
    public void testPriorityComparison() {
        assertEquals(0, RulePriority.HIGHEST - 1);
        assertEquals(0, RulePriority.HIGH - 5);
        assertEquals(0, RulePriority.MEDIUM - 10);
        assertEquals(0, RulePriority.SELECT_DEFAULT - 10);
        assertEquals(0, RulePriority.INSERT_DEFAULT - 20);
        assertEquals(0, RulePriority.UPDATE_DEFAULT - 30);
        assertEquals(0, RulePriority.DELETE_DEFAULT - 40);
        assertEquals(0, RulePriority.LOWEST - 50);
        assertEquals(0, RulePriority.DEFAULT - 100);
    }

    @Test
    public void testPriorityGaps() {
        int gap1 = RulePriority.HIGH - RulePriority.HIGHEST;
        int gap2 = RulePriority.MEDIUM - RulePriority.HIGH;
        int gap3 = RulePriority.LOW - RulePriority.MEDIUM;
        int gap4 = RulePriority.LOWEST - RulePriority.LOW;
        int gap5 = RulePriority.DEFAULT - RulePriority.LOWEST;

        assertEquals(4, gap1);
        assertEquals(5, gap2);
        assertEquals(10, gap3);
        assertEquals(30, gap4);
        assertEquals(50, gap5);
    }

    @Test
    public void testSqlTypePriorityGaps() {
        int gap1 = RulePriority.INSERT_DEFAULT - RulePriority.SELECT_DEFAULT;
        int gap2 = RulePriority.UPDATE_DEFAULT - RulePriority.INSERT_DEFAULT;
        int gap3 = RulePriority.DELETE_DEFAULT - RulePriority.UPDATE_DEFAULT;

        assertEquals(10, gap1);
        assertEquals(10, gap2);
        assertEquals(10, gap3);
    }

    @Test
    public void testAllConstantsArePositive() {
        assertTrue(RulePriority.HIGHEST > 0);
        assertTrue(RulePriority.HIGH > 0);
        assertTrue(RulePriority.MEDIUM > 0);
        assertTrue(RulePriority.LOW > 0);
        assertTrue(RulePriority.LOWEST > 0);
        assertTrue(RulePriority.DEFAULT > 0);
        assertTrue(RulePriority.SELECT_DEFAULT > 0);
        assertTrue(RulePriority.INSERT_DEFAULT > 0);
        assertTrue(RulePriority.UPDATE_DEFAULT > 0);
        assertTrue(RulePriority.DELETE_DEFAULT > 0);
    }

    @Test
    public void testPriorityLogicalOrder() {
        // 测试逻辑顺序：HIGHEST < HIGH < MEDIUM < LOW < LOWEST < DEFAULT
        assertTrue(RulePriority.HIGHEST < RulePriority.HIGH);
        assertTrue(RulePriority.HIGH < RulePriority.MEDIUM);
        assertTrue(RulePriority.MEDIUM < RulePriority.LOW);
        assertTrue(RulePriority.LOW < RulePriority.LOWEST);
        assertTrue(RulePriority.LOWEST < RulePriority.DEFAULT);
    }

    @Test
    public void testSqlTypePriorityLogicalOrder() {
        // 测试SQL类型优先级顺序：SELECT < INSERT < UPDATE < DELETE
        assertTrue(RulePriority.SELECT_DEFAULT <= RulePriority.INSERT_DEFAULT);
        assertTrue(RulePriority.INSERT_DEFAULT <= RulePriority.UPDATE_DEFAULT);
        assertTrue(RulePriority.UPDATE_DEFAULT <= RulePriority.DELETE_DEFAULT);
    }
}