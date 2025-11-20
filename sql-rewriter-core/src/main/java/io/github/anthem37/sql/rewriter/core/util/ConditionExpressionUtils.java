package io.github.anthem37.sql.rewriter.core.util;

import cn.hutool.core.util.ObjectUtil;
import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.EqualToConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.InConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsBooleanConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.expression.impl.IsNullConditionExpression;

import java.util.Collection;

/**
 * <p>
 * 条件表达式工具类
 * </p>
 *
 * @author anthem37
 * @since 2025/11/20 10:33:52
 */
public class ConditionExpressionUtils {

    /**
     * 创建自适应条件表达式
     * <p>
     * 根据列值的类型，创建相应的条件表达式。
     * 如果列值为null，则创建IS NULL条件表达式；
     * 如果列值为集合类型，则创建IN条件表达式；
     * 如果列值为boolean类型，则创建IS TRUE或IS FALSE条件表达式；
     * 否则，创建等值条件表达式。
     * </p>
     *
     * @param tableName   表名
     * @param columnName  字段名
     * @param columnValue 列值
     * @return 自适应的条件表达式
     */
    public static IConditionExpression createAdaptiveCondition(String tableName, String columnName, Object columnValue) {
        if (ObjectUtil.isNull(columnValue)) {
            return new IsNullConditionExpression(tableName, columnName);
        }
        // 判断是否为集合类型
        if (columnValue instanceof Collection) {
            return new InConditionExpression(tableName, columnName, (Collection<?>) columnValue);
        }
        // 判断是否为boolean类型
        if (columnValue instanceof Boolean) {
            return new IsBooleanConditionExpression(tableName, columnName, (Boolean) columnValue);
        }
        // 默认等值条件
        return new EqualToConditionExpression(tableName, columnName, columnValue);
    }

}
