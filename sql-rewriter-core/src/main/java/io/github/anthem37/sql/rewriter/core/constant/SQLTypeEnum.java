package io.github.anthem37.sql.rewriter.core.constant;

/**
 * SQL语句类型枚举
 *
 * <p>定义了SQL语句的基本类型，用于分类和处理不同的SQL操作。
 * 包括INSERT、DELETE、UPDATE、SELECT等常见类型。
 *
 * @author hb28301
 * @since 2025/11/20 20:28:50
 */
public enum SQLTypeEnum {
    /**
     * INSERT语句
     */
    INSERT,

    /**
     * DELETE语句
     */
    DELETE,

    /**
     * UPDATE语句
     */
    UPDATE,

    /**
     * SELECT语句
     */
    SELECT;
}
