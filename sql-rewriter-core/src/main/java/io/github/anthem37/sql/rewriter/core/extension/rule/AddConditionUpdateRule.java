package io.github.anthem37.sql.rewriter.core.extension.rule;

import cn.hutool.core.util.ObjectUtil;
import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
import io.github.anthem37.sql.rewriter.core.util.JsqlParserUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Update语句添加条件规则实现
 * <p>
 * 用于在SQL重写过程中，针对指定表名的UPDATE语句动态添加新的WHERE条件。
 * 支持批量更新、无WHERE条件更新等多种场景。
 * 典型应用场景：多租户数据隔离、数据审计等。
 * </p>
 *
 * @author hb28301
 * @since 2025/11/19 15:28:05
 */
@Getter
@AllArgsConstructor
public class AddConditionUpdateRule implements ISqlRule<Update> {

    /**
     * 目标表名（区分大小写，建议与SQL中表名保持一致）
     * 仅当SQL语句的主表或JOIN表名与此一致时才会应用本规则。
     */
    private final String tableName;

    /**
     * 条件表达式（如等值、范围等），会自动适配表别名
     * 通过IConditionExpression接口实现，支持灵活扩展。
     */
    private final IConditionExpression conditionExpression;

    /**
     * 规则优先级，数值越小优先级越高
     * 便于多规则协同时控制应用顺序。
     */
    private final int priority;

    /**
     * 构造函数，使用默认优先级（RulePriority.UPDATE_DEFAULT）
     *
     * @param tableName           目标表名
     * @param conditionExpression 条件表达式
     */
    public AddConditionUpdateRule(String tableName, IConditionExpression conditionExpression) {
        this(tableName, conditionExpression, RulePriority.UPDATE_DEFAULT);
    }

    @Override
    public Class<Update> getType() {
        return Update.class;
    }

    /**
     * 获取目标表名
     *
     * @return 目标表名
     */
    @Override
    public String getTargetTableName() {
        return tableName;
    }

    @Override
    public void applyTyped(Update statement) {
        Table table = statement.getTable();
        if (!matchTable(table)) {
            return;
        }
        String alias = JsqlParserUtils.getAlias(table);
        IConditionExpression reconstructAliasExpression = conditionExpression.reconstructAliasExpression(alias);
        Expression where = statement.getWhere();
        if (ObjectUtil.isNull(where)) {
            statement.setWhere(reconstructAliasExpression);
            return;
        }
        // 用括号包裹原有条件，避免OR优先级问题
        statement.setWhere(new AndExpression(new Parenthesis(where), reconstructAliasExpression));
    }

}
