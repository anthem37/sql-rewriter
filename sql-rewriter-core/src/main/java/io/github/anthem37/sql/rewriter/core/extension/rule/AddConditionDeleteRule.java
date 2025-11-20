package io.github.anthem37.sql.rewriter.core.extension.rule;

import cn.hutool.core.util.ObjectUtil;
import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import io.github.anthem37.sql.rewriter.core.rule.RulePriority;
import io.github.anthem37.sql.rewriter.core.util.JsqlParserUtils;
import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;

/**
 * 为DELETE语句添加条件的规则实现
 * <p>
 * 用于在SQL重写过程中，针对指定表名的DELETE语句动态添加WHERE条件。
 * 支持优先级配置，便于与其他规则协同工作。
 * 典型应用场景：多租户、数据权限等。
 *
 * @author anthem37
 * @since 2025/11/20 19:47:11
 */
@Getter
public class AddConditionDeleteRule implements ISqlRule<Delete> {

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
     * 构造函数，使用默认优先级（RulePriority.DELETE_DEFAULT）
     *
     * @param tableName           目标表名
     * @param conditionExpression 条件表达式
     */
    public AddConditionDeleteRule(String tableName, IConditionExpression conditionExpression) {
        this(tableName, conditionExpression, RulePriority.DELETE_DEFAULT);
    }

    /**
     * 构造函数，指定优先级
     *
     * @param tableName           目标表名
     * @param conditionExpression 条件表达式
     * @param priority            规则优先级
     */
    public AddConditionDeleteRule(String tableName, IConditionExpression conditionExpression, int priority) {
        this.tableName = tableName;
        this.conditionExpression = conditionExpression;
        this.priority = priority;
    }

    @Override
    public Class<Delete> getType() {
        return Delete.class;
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
    public void applyTyped(Delete statement) {
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
