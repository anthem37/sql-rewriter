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
import java.util.Collections;

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

    @Test
    public void aroundTenantMapping_shouldResolveTenantIdFromProviderAndClear() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoServiceWithProvider target = new DemoServiceWithProvider();

        Method method = DemoServiceWithProvider.class.getMethod("queryOrders");

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
                            TenantConfig config = TenantContext.get();
                            Assert.assertNotNull(config);
                            Assert.assertEquals(1, config.getConfigItems().size());

                            TenantConfig.ConfigItem item = config.getConfigItems().get(0);
                            Assert.assertEquals("t-provider", item.getSelectConditionColumnValue());
                            return "result";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                }
        );

        Object result = aspect.around(pjp);
        Assert.assertEquals("result", result);
        Assert.assertNull(TenantContext.get());
    }

    @Test
    public void aroundTenantMapping_shouldFindMethodLevelAnnotationFromImplementation() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoApiImpl target = new DemoApiImpl();

        Method method = DemoApi.class.getMethod("queryOrders");

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
                            TenantConfig config = TenantContext.get();
                            Assert.assertNotNull(config);
                            Assert.assertEquals(1, config.getConfigItems().size());

                            TenantConfig.ConfigItem item = config.getConfigItems().get(0);
                            Assert.assertEquals("t-100", item.getSelectConditionColumnValue());
                            Assert.assertEquals("orders", item.getTableNames().get(0));
                            return "result";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                }
        );

        Object result = aspect.around(pjp);
        Assert.assertEquals("result", result);
        Assert.assertNull(TenantContext.get());
    }

    @Test
    public void aroundTenantMapping_shouldFindAnnotationOnInterfaceMethod() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoApiAnnotatedOnInterface target = new DemoApiAnnotatedOnInterfaceImpl();

        Method method = DemoApiAnnotatedOnInterface.class.getMethod("queryOrders");

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
                            TenantConfig config = TenantContext.get();
                            Assert.assertNotNull(config);
                            Assert.assertEquals(1, config.getConfigItems().size());

                            TenantConfig.ConfigItem item = config.getConfigItems().get(0);
                            Assert.assertEquals("t-interface", item.getSelectConditionColumnValue());
                            Assert.assertEquals("orders", item.getTableNames().get(0));
                            return "result";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                }
        );

        Object result = aspect.around(pjp);
        Assert.assertEquals("result", result);
        Assert.assertNull(TenantContext.get());
    }

    @Test
    public void aroundTenantMapping_shouldFindAnnotationOnClass() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoClassAnnotated target = new DemoClassAnnotated();

        Method method = DemoClassAnnotated.class.getMethod("queryOrders");

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
                            TenantConfig config = TenantContext.get();
                            Assert.assertNotNull(config);
                            Assert.assertEquals(1, config.getConfigItems().size());

                            TenantConfig.ConfigItem item = config.getConfigItems().get(0);
                            Assert.assertEquals("t-class", item.getSelectConditionColumnValue());
                            Assert.assertEquals("orders", item.getTableNames().get(0));
                            return "result";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                }
        );

        Object result = aspect.around(pjp);
        Assert.assertEquals("result", result);
        Assert.assertNull(TenantContext.get());
    }

    @Test
    public void aroundTenantMapping_shouldSkipRewriteWhenTenantIdNull() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoClassWithNullTenantId target = new DemoClassWithNullTenantId();

        Method method = DemoClassWithNullTenantId.class.getMethod("queryOrders");

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
                            // 约束：tenantId 无法解析时，不写入 TenantContext，直接 proceed
                            Assert.assertNull(TenantContext.get());
                            return "result";
                        default:
                            return defaultValue(invokedMethod.getReturnType());
                    }
                }
        );

        Object result = aspect.around(pjp);
        Assert.assertEquals("result", result);
        Assert.assertNull(TenantContext.get());
    }

    @Test
    public void aroundTenantMapping_shouldRestoreOuterTenantContextWhenTenantIdNull() throws Throwable {
        TenantContextAspect aspect = new TenantContextAspect();
        DemoClassWithNullTenantIdForNested target = new DemoClassWithNullTenantIdForNested();

        TenantConfig oldConfig = new TenantConfig(Collections.singletonList(
                new TenantConfig.ConfigItem(
                        Collections.singletonList(SQLTypeEnum.SELECT),
                        Collections.singletonList("orders"),
                        "tenant_id",
                        () -> "old-tenant",
                        () -> "old-tenant",
                        () -> "old-tenant",
                        () -> "old-tenant",
                        1
                )
        ));

        TenantContext.set(oldConfig);
        try {
            Method method = DemoClassWithNullTenantIdForNested.class.getMethod("queryOrders");

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
                                // tenantId=null 时，内层 should 清空 TenantContext，避免触发 SQL 重写
                                Assert.assertNull(TenantContext.get());
                                return "result";
                            default:
                                return defaultValue(invokedMethod.getReturnType());
                        }
                    }
            );

            Object result = aspect.around(pjp);
            Assert.assertEquals("result", result);
            // tenantId=null 返回后应恢复到外层上下文
            Assert.assertSame(oldConfig, TenantContext.get());
        } finally {
            TenantContext.remove();
        }
    }

    interface DemoApi {
        String queryOrders();
    }

    interface DemoApiAnnotatedOnInterface {
        @TenantMapping(
                tenantId = @TenantId(value = "t-interface", tenantIdProvider = TenantIdProvider.class),
                tenantTargets = @TenantTargets({
                        @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id", sqlTypes = {SQLTypeEnum.SELECT}, priority = 1)
                })
        )
        String queryOrders();
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

    @TenantMapping(
            tenantId = @TenantId(tenantIdProvider = DemoProviderTenantId.class),
            tenantTargets = @TenantTargets({
                    @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id", sqlTypes = {SQLTypeEnum.SELECT}, priority = 1)
            })
    )
    static class DemoServiceWithProvider {
        public String queryOrders() {
            return "ok";
        }
    }

    public static class DemoProviderTenantId implements TenantIdProvider {
        @Override
        public Object getTenantId() {
            return "t-provider";
        }
    }

    public static class DemoProviderTenantIdNull implements TenantIdProvider {
        @Override
        public Object getTenantId() {
            return null;
        }
    }

    static class DemoApiImpl implements DemoApi {
        @Override
        @TenantMapping(
                tenantId = @TenantId(value = "t-100", tenantIdProvider = TenantIdProvider.class),
                tenantTargets = @TenantTargets({
                        @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id", sqlTypes = {SQLTypeEnum.SELECT}, priority = 1)
                })
        )
        public String queryOrders() {
            return "ok";
        }
    }

    static class DemoApiAnnotatedOnInterfaceImpl implements DemoApiAnnotatedOnInterface {
        @Override
        public String queryOrders() {
            return "ok";
        }
    }

    @TenantMapping(
            tenantId = @TenantId(value = "t-class", tenantIdProvider = TenantIdProvider.class),
            tenantTargets = @TenantTargets({
                    @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id", sqlTypes = {SQLTypeEnum.SELECT}, priority = 1)
            })
    )
    static class DemoClassAnnotated {
        public String queryOrders() {
            return "ok";
        }
    }

    @TenantMapping(
            tenantId = @TenantId(tenantIdProvider = DemoProviderTenantIdNull.class),
            tenantTargets = @TenantTargets({
                    @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id", sqlTypes = {SQLTypeEnum.SELECT}, priority = 1)
            })
    )
    static class DemoClassWithNullTenantId {
        public String queryOrders() {
            return "ok";
        }
    }

    @TenantMapping(
            tenantId = @TenantId(tenantIdProvider = DemoProviderTenantIdNull.class),
            tenantTargets = @TenantTargets({
                    @TenantTarget(tableNames = {"orders"}, columnName = "tenant_id", sqlTypes = {SQLTypeEnum.SELECT}, priority = 1)
            })
    )
    static class DemoClassWithNullTenantIdForNested {
        public String queryOrders() {
            return "ok";
        }
    }
}

