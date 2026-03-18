package io.github.anthem37.sql.rewriter.plugin.tenant.config;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * TenantConfig 单元测试
 *
 * @author anthem37
 * @since 2025/12/04
 */
public class TenantConfigTest {

    private TenantConfig tenantConfig;
    private TenantConfig.ConfigItem configItem;
    private List<SQLTypeEnum> sqlTypes;
    private List<String> tableNames;
    private String columnName;
    private Supplier<Object> insertValueSupplier;
    private Supplier<Object> deleteValueSupplier;
    private Supplier<Object> updateValueSupplier;
    private Supplier<Object> selectValueSupplier;
    private int priority;

    @Before
    public void setUp() {
        sqlTypes = Arrays.asList(SQLTypeEnum.SELECT, SQLTypeEnum.INSERT, SQLTypeEnum.UPDATE, SQLTypeEnum.DELETE);
        tableNames = Arrays.asList("user", "order", "product");
        columnName = "tenant_id";

        // 创建模拟的值提供者
        insertValueSupplier = () -> "insert-tenant-001";
        deleteValueSupplier = () -> "delete-tenant-001";
        updateValueSupplier = () -> "update-tenant-001";
        selectValueSupplier = () -> "select-tenant-001";

        priority = 10;

        configItem = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, priority
        );

        tenantConfig = new TenantConfig(Collections.singletonList(configItem));
    }

    @Test
    public void testTenantConfigCreation() {
        assertNotNull(tenantConfig);
        assertNotNull(tenantConfig.getConfigItems());
        assertEquals(1, tenantConfig.getConfigItems().size());
    }

    @Test
    public void testConfigItemCreation() {
        assertNotNull(configItem);
        assertEquals(sqlTypes, configItem.getRewritableSqlTypes());
        assertEquals(tableNames, configItem.getTableNames());
        assertEquals(columnName, configItem.getColumnName());
        assertEquals(insertValueSupplier, configItem.getInsertColumnValueSupplier());
        assertEquals(deleteValueSupplier, configItem.getDeleteConditionColumnValueSupplier());
        assertEquals(updateValueSupplier, configItem.getUpdateConditionColumnValueSupplier());
        assertEquals(selectValueSupplier, configItem.getSelectConditionColumnValueSupplier());
        assertEquals(priority, configItem.getPriority());
    }

    @Test
    public void testGetInsertColumnValue() {
        assertEquals("insert-tenant-001", configItem.getInsertColumnValue());
    }

    @Test
    public void testGetDeleteConditionColumnValue() {
        assertEquals("delete-tenant-001", configItem.getDeleteConditionColumnValue());
    }

    @Test
    public void testGetUpdateConditionColumnValue() {
        assertEquals("update-tenant-001", configItem.getUpdateConditionColumnValue());
    }

    @Test
    public void testGetSelectConditionColumnValue() {
        assertEquals("select-tenant-001", configItem.getSelectConditionColumnValue());
    }

    @Test
    public void testNullValueSuppliers() {
        TenantConfig.ConfigItem itemWithNullSuppliers = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, columnName,
                null, null, null, null, priority
        );

        assertNull(itemWithNullSuppliers.getInsertColumnValue());
        assertNull(itemWithNullSuppliers.getDeleteConditionColumnValue());
        assertNull(itemWithNullSuppliers.getUpdateConditionColumnValue());
        assertNull(itemWithNullSuppliers.getSelectConditionColumnValue());
    }

    @Test
    public void testEmptyTableNames() {
        TenantConfig.ConfigItem itemWithEmptyTableNames = new TenantConfig.ConfigItem(
                sqlTypes, Collections.emptyList(), columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, priority
        );

        assertTrue(itemWithEmptyTableNames.getTableNames().isEmpty());
    }

    @Test
    public void testEmptySqlTypes() {
        TenantConfig.ConfigItem itemWithEmptySqlTypes = new TenantConfig.ConfigItem(
                Collections.emptyList(), tableNames, columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, priority
        );

        assertTrue(itemWithEmptySqlTypes.getRewritableSqlTypes().isEmpty());
    }

    @Test
    public void testSingleSqlType() {
        TenantConfig.ConfigItem itemWithSingleSqlType = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT), tableNames, columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, priority
        );

        assertEquals(1, itemWithSingleSqlType.getRewritableSqlTypes().size());
        assertEquals(SQLTypeEnum.SELECT, itemWithSingleSqlType.getRewritableSqlTypes().get(0));
    }

    @Test
    public void testSingleTableName() {
        TenantConfig.ConfigItem itemWithSingleTableName = new TenantConfig.ConfigItem(
                sqlTypes, Collections.singletonList("single_table"), columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, priority
        );

        assertEquals(1, itemWithSingleTableName.getTableNames().size());
        assertEquals("single_table", itemWithSingleTableName.getTableNames().get(0));
    }

    @Test
    public void testDifferentPriorities() {
        TenantConfig.ConfigItem highPriorityItem = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, 1
        );

        TenantConfig.ConfigItem lowPriorityItem = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, columnName,
                insertValueSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, 100
        );

        assertTrue(highPriorityItem.getPriority() < lowPriorityItem.getPriority());
        assertEquals(1, highPriorityItem.getPriority());
        assertEquals(100, lowPriorityItem.getPriority());
    }

    @Test
    public void testSupplierThrowsException() {
        Supplier<Object> exceptionSupplier = () -> {
            throw new RuntimeException("模拟异常");
        };

        TenantConfig.ConfigItem itemWithExceptionSupplier = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, columnName,
                exceptionSupplier, deleteValueSupplier,
                updateValueSupplier, selectValueSupplier, priority
        );

        // supplier 异常应被捕获，避免影响整个 SQL 重写流程
        assertNull(itemWithExceptionSupplier.getInsertColumnValue());
    }

    @Test
    public void testMultipleConfigItems() {
        TenantConfig.ConfigItem secondConfigItem = new TenantConfig.ConfigItem(
                Collections.singletonList(SQLTypeEnum.SELECT),
                Collections.singletonList("second_table"),
                "second_column",
                () -> "second-value",
                () -> "second-value",
                () -> "second-value",
                () -> "second-value",
                20
        );

        TenantConfig multiItemConfig = new TenantConfig(Arrays.asList(configItem, secondConfigItem));

        assertEquals(2, multiItemConfig.getConfigItems().size());
        assertEquals(configItem, multiItemConfig.getConfigItems().get(0));
        assertEquals(secondConfigItem, multiItemConfig.getConfigItems().get(1));
    }

    @Test
    public void testConfigItemFieldValues() {
        // 测试不同类型的字段值
        Integer intValue = 123;
        String stringValue = "test-value";
        Boolean boolValue = true;

        TenantConfig.ConfigItem intItem = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, "int_column",
                () -> intValue, null, null, null, priority
        );

        TenantConfig.ConfigItem stringItem = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, "string_column",
                null, () -> stringValue, null, null, priority
        );

        TenantConfig.ConfigItem boolItem = new TenantConfig.ConfigItem(
                sqlTypes, tableNames, "bool_column",
                null, null, () -> boolValue, null, priority
        );

        assertEquals(Integer.valueOf(123), intItem.getInsertColumnValue());
        assertEquals("test-value", stringItem.getDeleteConditionColumnValue());
        assertEquals(Boolean.TRUE, boolItem.getUpdateConditionColumnValue());
        assertNull(boolItem.getSelectConditionColumnValue());
    }
}