package io.github.anthem37.sql.rewriter.plugin.tenant.rule;

import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewriter.core.extension.rule.*;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
import io.github.anthem37.sql.rewriter.core.util.ConditionExpressionUtils;
import io.github.anthem37.sql.rewriter.core.util.GsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户规则类
 * <p>
 * 定义了租户相关的SQL重写规则，用于在查询执行前对SQL语句进行修改，以实现多租户数据隔离。
 * 规则基于租户配置，动态添加WHERE条件，确保查询结果仅包含当前租户的数据。
 *
 * <p>
 * 规则执行流程：
 * <ol>
 *   <li>在INSERT语句中，为指定字段添加固定值，用于标识当前租户。</li>
 *   <li>在UPDATE和SELECT语句中，添加WHERE条件，过滤出当前租户的数据。</li>
 * </ol>
 *
 * @author anthem37
 * @since 2025/11/19 20:11:55
 */
@Getter
@Slf4j
public class TenantRule extends AbstractCombineSqlRule {

    /**
     * 目标表名
     */
    private final List<String> tableNames;

    /**
     * 字段名
     */
    private final String columnName;

    /**
     * 插入时字段值
     */
    private final Object insertColumnValue;

    /**
     * 删除时WHERE条件字段值
     */
    private final Object deleteConditionColumnValue;

    /**
     * 更新时WHERE条件字段值
     */
    private final Object updateConditionColumnValue;

    /**
     * 查询时WHERE条件字段值
     */
    private final Object selectConditionColumnValue;

    /**
     * 规则优先级，数值越小优先级越高
     */
    private final int priority;

    public TenantRule(List<String> tableNames, String columnName, Object columnValue) {
        this(tableNames, columnName, columnValue, RulePriority.INSERT_DEFAULT);
    }

    public TenantRule(List<String> tableNames, String columnName, Object columnValue, int priority) {
        this(tableNames, columnName, columnValue, columnValue, columnValue, priority);
    }

    public TenantRule(List<String> tableNames, String columnName, Object insertColumnValue, Object deleteConditionColumnValue, Object updateConditionColumnValue, Object selectConditionColumnValue) {
        this(tableNames, columnName, insertColumnValue, deleteConditionColumnValue, updateConditionColumnValue, selectConditionColumnValue, RulePriority.INSERT_DEFAULT);
    }

    public TenantRule(List<String> tableNames, String columnName, Object insertColumnValue, Object deleteConditionColumnValue, Object updateConditionColumnValue, Object selectConditionColumnValue, int priority) {
        super(tableNames.stream().flatMap(tableName -> Lists.newArrayList(
                        new AddColumnInsertRule(tableName, columnName, insertColumnValue),
                        new AddConditionDeleteRule(tableName, ConditionExpressionUtils.createAdaptiveCondition(tableName, columnName, deleteConditionColumnValue)),
                        new AddConditionUpdateRule(tableName, ConditionExpressionUtils.createAdaptiveCondition(tableName, columnName, updateConditionColumnValue)),
                        new AddConditionSelectRule(tableName, ConditionExpressionUtils.createAdaptiveCondition(tableName, columnName, selectConditionColumnValue)))
                .stream()).collect(Collectors.toList()));
        this.tableNames = tableNames;
        this.columnName = columnName;
        this.insertColumnValue = insertColumnValue;
        this.deleteConditionColumnValue = deleteConditionColumnValue;
        this.updateConditionColumnValue = updateConditionColumnValue;
        this.selectConditionColumnValue = selectConditionColumnValue;
        this.priority = priority;
        log.debug("创建租户规则: table={}, column={}, insertValue={}, deleteConditionValue={}, updateConditionValue={}, selectConditionValue={}, priority={}", GsonUtils.toJson(tableNames), columnName, insertColumnValue, deleteConditionColumnValue, updateConditionColumnValue, selectConditionColumnValue, priority);
    }

    @Override
    public boolean match(Statement statement) {

        return super.match(statement);
    }

    @Override
    public void apply(Statement statement) {
        super.apply(statement);
    }

}
