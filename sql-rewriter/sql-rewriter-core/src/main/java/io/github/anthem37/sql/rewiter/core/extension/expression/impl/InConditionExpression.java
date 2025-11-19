package io.github.anthem37.sql.rewiter.core.extension.expression.impl;

import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewiter.core.util.JsqlParserUtils;
import lombok.Getter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * IN条件表达式
 * 用于生成SQL中的IN条件，例如：column_name IN (value1, value2, ...)
 * </p>
 *
 * @author hb28301
 * @since 2025/11/19 13:47:29
 */
public class InConditionExpression extends InExpression implements IConditionExpression {

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
     * 值（支持任意类型，最终会转为SQL表达式）
     */
    @Getter
    private final List<Object> columnValue;

    /**
     * 构造IN条件表达式
     *
     * @param tableName   表名
     * @param columnName  字段名
     * @param columnValue 值列表
     */
    public InConditionExpression(String tableName, String columnName, List<Object> columnValue) {
        super();
        setLeftExpression(new Column(new Table(tableName), columnName));
        setRightExpression(new ExpressionList<>(columnValue.stream().map(JsqlParserUtils::createValueExpression).collect(Collectors.toList())));
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnValue = columnValue;
    }

    /**
     * 生成适配别名的新条件表达式
     * <p>
     * 用于SQL重写时，将表名替换为别名，保证条件表达式与AST结构一致。
     * </p>
     *
     * @param alias 别名
     * @return 新等值条件表达式
     */
    @Override
    public IConditionExpression reconstructAliasExpression(String alias) {
        return new InConditionExpression(alias, columnName, columnValue);
    }
    
}
