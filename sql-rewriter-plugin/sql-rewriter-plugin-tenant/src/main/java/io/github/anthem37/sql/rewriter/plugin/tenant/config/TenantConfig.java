package io.github.anthem37.sql.rewriter.plugin.tenant.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

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
     * 字段值
     */
    private final Object columnValue;

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
}
