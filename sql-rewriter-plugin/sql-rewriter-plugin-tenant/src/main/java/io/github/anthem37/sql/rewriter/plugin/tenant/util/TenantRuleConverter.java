package io.github.anthem37.sql.rewriter.plugin.tenant.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddColumnInsertRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionDeleteRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionSelectRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionUpdateRule;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.core.util.ConditionExpressionUtils;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.rule.TenantRule;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户规则转换器
 * <p>
 * 提供租户配置转换为租户规则的工具方法。
 * </p>
 *
 * @author anthem37
 * @since 2025/12/04
 */
public class TenantRuleConverter {

    /**
     * 将租户配置转换为租户规则
     *
     * @param tenantConfig 租户配置对象，不能为null
     * @return 对应的租户规则对象
     */
    public static TenantRule convertToTenantRule(TenantConfig tenantConfig) {
        return new TenantRule(tenantConfig);
    }

    /**
     * 将租户配置项转换为SQL规则
     *
     * @param configItem 租户配置项对象，不能为null
     * @return 对应的SQL规则对象列表
     */
    public static List<ISqlRule<?>> convertToSqlRules(TenantConfig.ConfigItem configItem) {
        List<ISqlRule<?>> sqlRules = Lists.newArrayList();
        if (ObjectUtil.isNull(configItem) || CollectionUtil.isEmpty(configItem.getRewritableSqlTypes())) {
            return sqlRules;
        }
        List<SQLTypeEnum> rewritableSqlTypes = configItem.getRewritableSqlTypes();
        List<String> tableNames = configItem.getTableNames();
        if (CollectionUtil.isEmpty(tableNames)) {
            return sqlRules;
        }
        if (rewritableSqlTypes.contains(SQLTypeEnum.INSERT)) {
            List<AddColumnInsertRule> addColumnInsertRules = tableNames.stream()
                    .map(tableName -> new AddColumnInsertRule(tableName, configItem.getColumnName(), configItem.getInsertColumnValue()))
                    .collect(Collectors.toList());
            sqlRules.addAll(addColumnInsertRules);
        }
        if (rewritableSqlTypes.contains(SQLTypeEnum.DELETE)) {
            List<AddConditionDeleteRule> addConditionDeleteRules = tableNames.stream()
                    .map(tableName -> new AddConditionDeleteRule(tableName, ConditionExpressionUtils.createAdaptiveCondition(tableName, configItem.getColumnName(), configItem.getDeleteConditionColumnValue())))
                    .collect(Collectors.toList());
            sqlRules.addAll(addConditionDeleteRules);
        }
        if (rewritableSqlTypes.contains(SQLTypeEnum.UPDATE)) {
            List<AddConditionUpdateRule> addConditionUpdateRules = tableNames.stream()
                    .map(tableName -> new AddConditionUpdateRule(tableName, ConditionExpressionUtils.createAdaptiveCondition(tableName, configItem.getColumnName(), configItem.getUpdateConditionColumnValue())))
                    .collect(Collectors.toList());
            sqlRules.addAll(addConditionUpdateRules);
        }
        if (rewritableSqlTypes.contains(SQLTypeEnum.SELECT)) {
            List<AddConditionSelectRule> addConditionSelectRules = tableNames.stream()
                    .map(tableName -> new AddConditionSelectRule(tableName, ConditionExpressionUtils.createAdaptiveCondition(tableName, configItem.getColumnName(), configItem.getSelectConditionColumnValue())))
                    .collect(Collectors.toList());
            sqlRules.addAll(addConditionSelectRules);
        }
        return sqlRules;
    }
}