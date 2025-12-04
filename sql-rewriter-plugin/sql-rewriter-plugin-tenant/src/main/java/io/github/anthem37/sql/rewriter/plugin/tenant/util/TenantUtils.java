package io.github.anthem37.sql.rewriter.plugin.tenant.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddColumnInsertRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionDeleteRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionSelectRule;
import io.github.anthem37.sql.rewriter.core.extension.rule.AddConditionUpdateRule;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.core.util.ConditionExpressionUtils;
import io.github.anthem37.sql.rewriter.core.util.GsonUtils;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.rule.TenantRule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租户工具类
 * <p>
 * 提供租户相关的工具方法，如将租户配置转换为租户规则。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/19 20:10:52
 */
public class TenantUtils {

    /**
     * 将租户配置转换为租户规则
     *
     * @param tenantConfig 租户配置对象，不能为null
     * @return 对应的租户规则对象
     */
    public static TenantRule convert2TenantRule(TenantConfig tenantConfig) {

        return new TenantRule(tenantConfig);
    }

    /**
     * 将租户配置项转换为SQL规则
     *
     * @param configItem 租户配置项对象，不能为null
     * @return 对应的SQL规则对象列表
     */
    public static List<ISqlRule<?>> convert2TenantItemSqlRules(TenantConfig.ConfigItem configItem) {
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

    /**
     * 租户配置持有者
     *
     * @author anthem37
     * @since 2025/11/19 20:03:42
     */
    @Slf4j
    public static class TenantConfigHolder {

        /**
         * ThreadLocal存储，确保每个线程的租户配置相互隔离
         */
        private static final TransmittableThreadLocal<TenantConfig> HOLDER = new TransmittableThreadLocal<>();

        /**
         * 设置当前线程的租户配置
         *
         * <p>将租户配置存储到当前线程的ThreadLocal中。
         * 该配置只对当前线程可见，不会影响其他线程。
         *
         * @param config 租户配置对象，不能为null
         */
        public static void set(TenantConfig config) {
            TenantConfig oldConfig = HOLDER.get();
            HOLDER.set(config);

            if (Objects.isNull(oldConfig)) {
                log.debug("设置租户配置: newConfig={}", GsonUtils.toJson(config));
                return;
            }
            log.debug("更新租户配置: 从 oldConfig={} 更新为 newConfig={}", GsonUtils.toJson(oldConfig), GsonUtils.toJson(config));
        }

        /**
         * 获取当前线程的租户配置
         *
         * <p>从当前线程的ThreadLocal中获取租户配置。
         * 如果当前线程没有设置租户配置，则返回null。
         *
         * @return 当前线程的租户配置，如果没有设置则返回null
         */
        public static TenantConfig get() {

            return HOLDER.get();
        }

        /**
         * 移除当前线程的租户配置
         *
         * <p>清理当前线程的ThreadLocal中的租户配置，释放内存。
         * 必须在方法结束时调用，避免内存泄漏。
         *
         * <p>注意：通常由切面自动调用，无需手动调用。
         */
        public static void remove() {
            TenantConfig config = HOLDER.get();
            if (Objects.nonNull(config)) {
                log.debug("移除租户配置: config={}", GsonUtils.toJson(config));
            }
            HOLDER.remove();
        }

    }

}
