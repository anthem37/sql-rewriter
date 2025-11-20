package io.github.anthem37.sql.rewriter.plugin.tenant.rule;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.core.extension.rule.AbstractCombineSqlRule;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.core.util.GsonUtils;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantUtils;
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
     * 租户规则项列表
     */
    private final List<TenantRuleItem> tenantRuleItems;

    /**
     * 所有SQL规则的列表
     */
    private final List<ISqlRule<?>> rules;

    /**
     * 构造函数
     *
     * @param tenantConfig 租户配置
     */
    public TenantRule(TenantConfig tenantConfig) {
        this.tenantRuleItems = tenantConfig.getConfigItems().stream().map(TenantRuleItem::new).collect(Collectors.toList());
        this.rules = tenantRuleItems.stream().flatMap(item -> item.getRules().stream()).collect(Collectors.toList());
    }


    @Getter
    @Slf4j
    public static class TenantRuleItem extends AbstractCombineSqlRule {
        /**
         * 可以被改写的SQL类型列表
         */
        private final List<SQLTypeEnum> rewritableSqlTypes;

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

        private final List<ISqlRule<?>> rules;

        /**
         * 规则优先级，数值越小优先级越高
         */
        private final int priority;

        public TenantRuleItem(TenantConfig.ConfigItem configItem) {
            this.rewritableSqlTypes = configItem.getRewritableSqlTypes();
            this.tableNames = configItem.getTableNames();
            this.columnName = configItem.getColumnName();
            this.insertColumnValue = configItem.getInsertColumnValue();
            this.deleteConditionColumnValue = configItem.getDeleteConditionColumnValue();
            this.updateConditionColumnValue = configItem.getUpdateConditionColumnValue();
            this.selectConditionColumnValue = configItem.getSelectConditionColumnValue();
            this.priority = configItem.getPriority();
            this.rules = TenantUtils.convert2TenantItemSqlRules(configItem);
            log.debug("创建租户规则项: table={}, column={}, insertValue={}, deleteConditionValue={}, updateConditionValue={}, selectConditionValue={}, priority={}", GsonUtils.toJson(tableNames), columnName, insertColumnValue, deleteConditionColumnValue, updateConditionColumnValue, selectConditionColumnValue, priority);
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

}
