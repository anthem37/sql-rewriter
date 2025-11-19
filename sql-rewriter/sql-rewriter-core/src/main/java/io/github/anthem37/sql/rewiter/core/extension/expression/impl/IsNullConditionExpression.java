package io.github.anthem37.sql.rewiter.core.extension.expression.impl;

import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import lombok.Getter;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * <p>
 * IS NULL条件表达式
 * 用于生成SQL中的IS NULL条件，例如：column_name IS NULL
 * </p>
 *
 * @author hb28301
 * @since 2025/11/18 19:56:43
 */
public class IsNullConditionExpression extends IsNullExpression implements IConditionExpression {

    /**
     * 表名（区分大小写，建议与SQL中表名保持一致）
     * 用于生成左侧的Column对象，支持别名适配。
     */
    @Getter
    private final String tableName;

    /**
     * 字段名
     */
    @Getter
    private final String columnName;

    /**
     * 构造IS NULL条件表达式
     *
     * @param tableName  表名
     * @param columnName 字段名
     */
    public IsNullConditionExpression(String tableName, String columnName) {
        super();
        setLeftExpression(new Column(new Table(tableName), columnName));
        this.tableName = tableName;
        this.columnName = columnName;
    }

}
