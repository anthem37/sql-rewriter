package io.github.anthem37.sql.rewriter.core.extension.visitor.impl;

import cn.hutool.core.collection.CollectionUtil;
import io.github.anthem37.sql.rewriter.core.extension.expression.IConditionExpression;
import io.github.anthem37.sql.rewriter.core.extension.visitor.IAddConditionFromItemVisitor;
import io.github.anthem37.sql.rewriter.core.util.JsqlParserUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

/**
 * Select FromItem条件递归访问器实现
 * <p>
 * 用于递归遍历SELECT语句的FromItem（如子查询、表、嵌套结构），
 * 动态为目标表添加条件表达式。
 * 典型应用场景：多租户、数据权限等。
 * </p>
 *
 * @author hb28301
 * @since 2025/11/12 15:28:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AddConditionFromItemVisitor extends FromItemVisitorAdapter implements IAddConditionFromItemVisitor {

    /**
     * 目标表名（区分大小写，建议与SQL中表名保持一致）
     */
    private final String tableName;
    /**
     * 条件表达式（如等值、范围等），会自动适配表别名
     */
    private final IConditionExpression conditionExpression;

    /**
     * 访问括号中的SELECT语句，递归添加条件
     *
     * @param selectBody 括号中的SELECT语句
     */
    @Override
    public void visit(ParenthesedSelect selectBody) {
        AddConditionSelectVisitor addConditionSelectVisitor = new AddConditionSelectVisitor(tableName, conditionExpression);
        selectBody.accept(addConditionSelectVisitor);
    }

    /**
     * 访问括号中的FROM子句，递归添加条件
     *
     * @param aThis 括号中的FROM子句
     */
    @Override
    public void visit(ParenthesedFromItem aThis) {
        FromItem leftFromItem = aThis.getFromItem();
        boolean leftMatched = false;
        String leftAlias = null;
        if (leftFromItem instanceof Table) {
            Table table = ((Table) leftFromItem);
            leftMatched = JsqlParserUtils.equalToTableName(tableName, table);
            if (leftMatched) {
                leftAlias = JsqlParserUtils.getAlias(table);
            }
        }
        leftFromItem.accept(this);
        List<Join> joins = aThis.getJoins();
        if (CollectionUtil.isEmpty(joins)) {
            return;
        }
        for (int i = 0; i < joins.size(); i++) {
            Join join = joins.get(i);
            FromItem rightItem = join.getRightItem();
            // 只对“当前 join 右表匹配”的 JOIN 注入条件，避免错误扩散到后续不相关 JOIN 上。
            String joinAlias = null;
            if (rightItem instanceof Table) {
                Table table = (Table) rightItem;
                if (JsqlParserUtils.equalToTableName(tableName, table)) {
                    joinAlias = JsqlParserUtils.getAlias(table);
                }
            }
            if (joinAlias != null) {
                IConditionExpression aliasConditionExpression = conditionExpression.reconstructAliasExpression(joinAlias);
                addAndExpression4Join(join, aliasConditionExpression);
            } else if (i == 0 && leftMatched) {
                // 仅当目标表命中左侧 FROM 时，给“第一条 JOIN”注入条件；
                // 比起对后续所有 JOIN 注入更安全，且能覆盖 left-most table 的约束。
                IConditionExpression aliasConditionExpression = conditionExpression.reconstructAliasExpression(leftAlias);
                addAndExpression4Join(join, aliasConditionExpression);
            }
            rightItem.accept(this);
        }
    }

}

