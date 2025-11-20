package io.github.anthem37.sql.rewriter.core.extension.rule;

import io.github.anthem37.sql.rewriter.core.rule.ICombineSqlRule;
import io.github.anthem37.sql.rewriter.core.rule.ISqlRule;
import lombok.Getter;
import net.sf.jsqlparser.statement.Statement;

/**
 * 组合SQL规则抽象类
 *
 * @author anthem37
 * @since 2025/11/13 14:26:51
 */
@Getter
public abstract class AbstractCombineSqlRule implements ICombineSqlRule {

    @Override
    public boolean match(Statement statement) {

        return getRules().stream().anyMatch(sqlRule -> sqlRule.match(statement));
    }

    @Override
    public void apply(Statement statement) {
        for (ISqlRule<?> rule : getRules()) {
            rule.apply(statement);
        }
    }

}
