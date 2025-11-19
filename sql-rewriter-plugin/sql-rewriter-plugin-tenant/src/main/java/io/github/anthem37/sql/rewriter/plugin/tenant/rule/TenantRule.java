package io.github.anthem37.sql.rewriter.plugin.tenant.rule;

import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.rule.AbstractCombineSqlRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddColumnInsertRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionSelectRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionUpdateRule;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
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
     * 值
     */
    private final Object columnValue;

    /**
     * 规则优先级，数值越小优先级越高
     */
    private final int priority;

    public TenantRule(List<String> tableNames, String columnName, Object columnValue) {
        this(tableNames, columnName, columnValue, RulePriority.INSERT_DEFAULT);
    }

    public TenantRule(List<String> tableNames, String columnName, Object columnValue, int priority) {
        super(tableNames.stream().flatMap(tableName -> Lists.newArrayList(
                new AddColumnInsertRule(tableName, columnName, columnValue),
                new AddConditionSelectRule(tableName, new EqualToConditionExpression(tableName, columnName, columnValue)),
                new AddConditionUpdateRule(tableName, new EqualToConditionExpression(tableName, columnName, columnValue))
        ).stream()).collect(Collectors.toList()));
        this.tableNames = tableNames;
        this.columnName = columnName;
        this.columnValue = columnValue;
        this.priority = priority;
        log.debug("创建租户规则: table={}, column={}, value={}, priority={}", GsonUtils.toJson(tableNames), columnName, columnValue, priority);
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
