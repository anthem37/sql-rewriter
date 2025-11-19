package io.github.anthem37.sql.rewiter.core.extension.expression.impl;

import io.github.anthem37.sql.rewiter.core.extension.expression.IConditionExpression;
import lombok.Getter;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

/**
 * 表示 IS TRUE 或 IS FALSE 表达式
 *
 * @author hb28301
 * @since 2025/11/19 14:36:25
 */
public class IsBooleanConditionExpression extends IsBooleanExpression implements IConditionExpression {

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
     * 是否为NOT条件
     */
    @Getter
    private final Boolean not;


    /**
     * 是否为TRUE条件
     */
    @Getter
    private final Boolean isTrue;

    /**
     * 构造IS TRUE或IS FALSE条件表达式
     *
     * @param tableName  表名
     * @param columnName 字段名
     * @param isTrue     是否为TRUE
     */
    public IsBooleanConditionExpression(String tableName, String columnName, boolean not, boolean isTrue) {
        super();
        setLeftExpression(new Column(new Table(tableName), columnName));
        setIsTrue(isTrue);
        setNot(not);
        this.tableName = tableName;
        this.columnName = columnName;
        this.not = not;
        this.isTrue = isTrue;
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
        return new IsBooleanConditionExpression(alias, columnName, not, isTrue);
    }

}
