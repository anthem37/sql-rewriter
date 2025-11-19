package io.github.anthem37.sql.rewriter.plugin.tenant.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.engine.TenantEngine;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * 租户SQL重写拦截器
 *
 * @author anthem37
 * @since 2025/11/19 20:09:05
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class TenantSqlRewriteInterceptor implements Interceptor {

    /**
     * 租户引擎提供者
     *
     * <p>懒加载方式创建租户引擎，只有在需要时才从ThreadLocal中获取租户配置并创建引擎。
     * 如果当前线程没有租户配置，则返回null，表示不需要进行SQL重写。
     */
    private final Supplier<TenantEngine> tenantEngineSupplier = () -> {
        TenantConfig tenantConfig = TenantUtils.TenantConfigHolder.get();
        if (ObjectUtil.isEmpty(tenantConfig)) {
            return null;
        }
        return new TenantEngine(Lists.newArrayList(tenantConfig));
    };

    /**
     * 拦截SQL执行，进行租户SQL重写
     *
     * <p>执行流程：
     * <ol>
     *   <li>检查租户功能开关是否启用</li>
     *   <li>获取租户引擎（如果当前线程有租户配置）</li>
     *   <li>如果没有租户配置或引擎为空，直接执行原SQL</li>
     *   <li>通过租户引擎对SQL进行重写，添加租户过滤条件</li>
     *   <li>使用重写后的SQL执行查询</li>
     * </ol>
     *
     * @param invocation MyBatis拦截器调用上下文
     * @return 查询结果
     * @throws Throwable 执行异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();

        TenantEngine tenantEngine = tenantEngineSupplier.get();
        if (ObjectUtil.isEmpty(tenantEngine)) {
            return invocation.proceed();
        }

        // 直接全部重写，由tenantEngine决定是否处理
        String newSql = tenantEngine.run(originalSql);
        metaObject.setValue("boundSql.sql", newSql);
        return invocation.proceed();
    }

    /**
     * 创建代理对象
     *
     * @param target 目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置拦截器属性
     *
     * <p>当前实现为空，因为该拦截器不需要额外的配置属性。
     * 租户配置通过ThreadLocal和配置项进行管理。
     *
     * @param properties 属性配置
     */
    @Override
    public void setProperties(Properties properties) {
        // 无需实现
    }
}

