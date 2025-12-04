package io.github.anthem37.sql.rewriter.plugin.tenant.config;

import cn.hutool.core.util.ObjectUtil;
import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Supplier;

/**
 * 租户配置类
 *
 * <p>用于定义租户过滤条件的数据库表名、字段名、字段值和优先级。
 *
 * @author anthem37
 * @since 2025/11/19 20:01:44
 */
@Data
@AllArgsConstructor
public class TenantConfig {

    /**
     * 租户配置项列表
     *
     * <p>包含多个 {@link ConfigItem} 实例，每个实例定义了一个数据库表的租户过滤条件。
     */
    private final List<ConfigItem> configItems;

    @Data
    @AllArgsConstructor
    public static class ConfigItem {

        /**
         * 可以被改写的SQL类型列表
         *
         * <p>指定要应用本配置项的SQL语句类型。
         * 支持 INSERT、DELETE、UPDATE、SELECT 等。
         */
        private final List<SQLTypeEnum> rewritableSqlTypes;

        /**
         * 目标表名
         *
         * <p>指定要添加租户过滤条件的数据库表名。
         * 例如：user_info、order_detail、product_list 等。
         */
        private final List<String> tableNames;

        /**
         * 字段名
         *
         * <p>指定要添加租户过滤条件的数据库字段名。
         * 通常为租户标识字段，如 tenant_id、tenant_code、org_id 等。
         */
        private final String columnName;

        /**
         * 插入时字段值提供函数
         *
         * <p>用于动态提供插入语句中租户字段的实际值。
         * 例如，从当前登录用户上下文获取当前租户 ID。
         */
        private final Supplier<?> insertColumnValueSupplier;

        /**
         * 删除时WHERE条件字段值提供函数
         *
         * <p>用于动态提供删除语句中租户过滤条件的实际值。
         * 例如，从当前登录用户上下文获取当前租户 ID。
         */
        private final Supplier<?> deleteConditionColumnValueSupplier;

        /**
         * 更新时WHERE条件字段值提供函数
         *
         * <p>用于动态提供更新语句中租户过滤条件的实际值。
         * 例如，从当前登录用户上下文获取当前租户 ID。
         */
        private final Supplier<?> updateConditionColumnValueSupplier;

        /**
         * 查询时WHERE条件字段值提供函数
         *
         * <p>用于动态提供查询语句中租户过滤条件的实际值。
         * 例如，从当前登录用户上下文获取当前租户 ID。
         */
        private final Supplier<?> selectConditionColumnValueSupplier;

        /**
         * 规则优先级
         *
         * <p>当存在多个租户配置时，用于确定执行顺序。
         * 数值越小优先级越高，优先级高的配置会先执行。
         *
         * <p>常用优先级：
         * <ul>
         *   <li>1：最高优先级，通常用于核心业务表</li>
         *   <li>5：中等优先级，通常用于一般业务表</li>
         *   <li>10：最低优先级，通常用于辅助表</li>
         * </ul>
         */
        private final int priority;

        /**
         * 获取插入时字段值
         *
         * <p>通过调用字段值提供函数获取实际的租户字段值。
         * 如果提供函数为 {@code null}，则返回 {@code null}。
         *
         * @return 租户字段值，或 {@code null} 如果提供函数为 {@code null}
         */
        public Object getInsertColumnValue() {
            if (ObjectUtil.isNull(insertColumnValueSupplier)) {
                return null;
            }
            try {
                return insertColumnValueSupplier.get();
            } catch (Exception e) {
                // 记录异常但不传播，避免影响整个SQL重写流程
                System.err.println("获取插入时字段值失败: " + e.getMessage());
                return null;
            }
        }

        /**
         * 获取删除时WHERE条件字段值
         *
         * <p>通过调用字段值提供函数获取实际的租户过滤条件值。
         * 如果提供函数为 {@code null}，则返回 {@code null}。
         *
         * @return 租户过滤条件值，或 {@code null} 如果提供函数为 {@code null}
         */
        public Object getDeleteConditionColumnValue() {
            if (ObjectUtil.isNull(deleteConditionColumnValueSupplier)) {
                return null;
            }
            try {
                return deleteConditionColumnValueSupplier.get();
            } catch (Exception e) {
                // 记录异常但不传播，避免影响整个SQL重写流程
                System.err.println("获取删除时WHERE条件字段值失败: " + e.getMessage());
                return null;
            }
        }

        /**
         * 获取更新时WHERE条件字段值
         *
         * <p>通过调用字段值提供函数获取实际的租户过滤条件值。
         * 如果提供函数为 {@code null}，则返回 {@code null}。
         *
         * @return 租户过滤条件值，或 {@code null} 如果提供函数为 {@code null}
         */
        public Object getUpdateConditionColumnValue() {
            if (ObjectUtil.isNull(updateConditionColumnValueSupplier)) {
                return null;
            }
            try {
                return updateConditionColumnValueSupplier.get();
            } catch (Exception e) {
                // 记录异常但不传播，避免影响整个SQL重写流程
                System.err.println("获取更新时WHERE条件字段值失败: " + e.getMessage());
                return null;
            }
        }

        /**
         * 获取查询时WHERE条件字段值
         *
         * <p>通过调用字段值提供函数获取实际的租户过滤条件值。
         * 如果提供函数为 {@code null}，则返回 {@code null}。
         *
         * @return 租户过滤条件值，或 {@code null} 如果提供函数为 {@code null}
         */
        public Object getSelectConditionColumnValue() {
            if (ObjectUtil.isNull(selectConditionColumnValueSupplier)) {
                return null;
            }
            try {
                return selectConditionColumnValueSupplier.get();
            } catch (Exception e) {
                // 记录异常但不传播，避免影响整个SQL重写流程
                System.err.println("获取查询时WHERE条件字段值失败: " + e.getMessage());
                return null;
            }
        }
    }

}
