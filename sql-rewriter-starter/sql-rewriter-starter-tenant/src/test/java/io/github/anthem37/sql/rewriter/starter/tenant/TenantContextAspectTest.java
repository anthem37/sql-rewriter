package io.github.anthem37.sql.rewriter.starter.tenant;

import io.github.anthem37.sql.rewriter.core.constant.SQLTypeEnum;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContext;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantId;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantMapping;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantTarget;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantTargets;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantColumnNameProvider;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantContextAspect;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantIdProvider;
import io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor.TenantTableNamesProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * {@link TenantContextAspect} 单元测试。
 *
 * @author anthem37
 * @since 2026/03/19
 */
public class TenantContextAspectTest {

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        return null;
    }

    @Test
    public void aroundTenantMapping_shouldSetAndClearTenantContext() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoService target = new DemoService();

        Method method = DemoService.class.getMethod("queryOrders");

        MethodSignature methodSignature = (MethodSignature) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{MethodSignature.class},
                (proxy, invokedMethod, args) -> {
                    if ("getMethod".equals(invokedMethod.getName())) {
                        return method;
                    }
                    return defaultValue(invokedMethod.getReturnType());
                }
        );

        ProceedingJoinPoint pjp = (ProceedingJoinPoint) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ProceedingJoinPoint.class},
                (proxy, invokedMethod, args) -> {
                    switch (invokedMethod.getName()) {
                        case "getSignature":
                            return methodSignature;
                        case "getTarget":
                            return target;
                        case "proceed":
                            // 断言：around 执行期间，TenantContext 已被写入
                            TenantConfig config = TenantContext.get();
                            Assert.assertNotNull(config);
                            Assert.assertEquals(1, config.getConfigItems().size());

                            TenantConfig.ConfigItem item = config.getConfigItems().get(0);
                            Assert.assertEquals(1, item.getPriority());
                            Assert.assertEquals("tenant_id", item.getColumnName());
                            Assert.assertEquals("t-001", item.getSelectConditionColumnValue());
                            Assert.assertEquals(1, item.getTableNames().size());
                            Assert.assertEquals("orders", item.getTableNames().get(0));
                            Assert.assertEquals(1, item.getRewritableSqlTypes().size());
                            Assert.assertEquals(SQLTypeEnum.SELECT, item.getRewritableSqlTypes().get(0));

                            return "result";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                }
        );

        Object result = aspect.around(pjp);
        Assert.assertEquals("result", result);

        // 断言：around 返回后，TenantContext 已被清理
        Assert.assertNull(TenantContext.get());
    }

    @TenantMapping(
            tenantId = @TenantId(value = "t-001", tenantIdProvider = TenantIdProvider.class),
            tenantTargets = @TenantTargets({
                    @TenantTarget(
                            tableNames = {"orders"},
                            columnName = "tenant_id",
                            sqlTypes = {SQLTypeEnum.SELECT},
                            priority = 1,
                            tableNamesProvider = TenantTableNamesProvider.class,
                            columnNameProvider = TenantColumnNameProvider.class
                    )
            })
    )
    static class DemoService {
        public String queryOrders() {
            return "ok";
        }
    }
}

