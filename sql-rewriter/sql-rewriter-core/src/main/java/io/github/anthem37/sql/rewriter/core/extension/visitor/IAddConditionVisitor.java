package io.github.anthem37.sql.rewriter.core.extension.visitor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.Collection;

/**
 * 添加条件表达式访问器接口
 * <p>
 * 用于在SQL AST中添加新的条件表达式。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/12 15:20:42
 */
public interface IAddConditionVisitor {

    /**
     * 给where添加and连接的条件
     *
     * @param plainSelect 查询
     * @param expression  条件表达式
     */
    default void addAndExpression4Where(PlainSelect plainSelect, Expression expression) {
        Expression where = plainSelect.getWhere();
        if (ObjectUtil.isEmpty(where)) {
            plainSelect.setWhere(expression);
        } else {
            // 用括号包裹原有条件，避免OR优先级问题
            plainSelect.setWhere(new AndExpression(new Parenthesis(where), expression));
        }
    }

    /**
     * 给join添加and连接的条件
     *
     * @param join       join
     * @param expression 条件表达式
     */
    default void addAndExpression4Join(Join join, Expression expression) {
        Collection<Expression> onExpressions = join.getOnExpressions();
        if (CollectionUtil.isEmpty(onExpressions)) {
            join.addOnExpression(expression);
            return;
        }
        ExpressionList onExpressionList = new ExpressionList(onExpressions);
        // 用括号包裹原有条件，避免OR优先级问题
        AndExpression newOnExpression = new AndExpression(new Parenthesis(onExpressionList), expression);
        join.setOnExpressions(Lists.newArrayList(newOnExpression));
    }

}
