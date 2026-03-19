package io.github.anthem37.sql.rewriter.starter.tenant.support.interceptor;

import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContext;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantContextHolder;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantId;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantMapping;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantTarget;
import io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantTargets;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 在方法调用期间向 {@link TenantContextHolder} 写入租户配置。
 *
 * <p>约束：本切面只识别 {@link io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantMapping}
 * 作为唯一入口，从而保证注解使用方式的可读性与一致性。</p>
 *
 * @author anthem37
 * @since 2026/03/19
 */
@Slf4j
@Aspect
public class TenantContextAspect {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Around("@annotation(io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantMapping) || " +
            "@within(io.github.anthem37.sql.rewriter.starter.tenant.annotation.TenantMapping)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = pjp.getTarget() == null ? null : pjp.getTarget().getClass();

        Method mostSpecificMethod = method;
        if (targetClass != null) {
            mostSpecificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        }

        TenantMapping mapping = AnnotationUtils.findAnnotation(mostSpecificMethod, TenantMapping.class);
        if (mapping == null) {
            // 兜底：注解可能直接标在接口方法上（实现类未显式标注）
            mapping = AnnotationUtils.findAnnotation(method, TenantMapping.class);
        }
        if (mapping == null && targetClass != null) {
            mapping = AnnotationUtils.findAnnotation(targetClass, TenantMapping.class);
        }

        if (mapping == null) {
            return pjp.proceed();
        }

        TenantId tenantIdAnn = mapping.tenantId();
        TenantTargets tenantTargets = mapping.tenantTargets();

        // 1) resolve tenantId
        Object tenantId = resolveTenantId(tenantIdAnn);
        // 约束：tenantId 无法解析时，跳过本次 SQL 重写。
        //    为避免嵌套调用时复用到上层的 TenantContext，这里会在 proceed 期间临时清空 TenantContext。
        if (tenantId == null) {
            TenantConfig oldConfig = TenantContext.get();
            TenantContext.remove();
            try {
                return pjp.proceed();
            } finally {
                if (oldConfig != null) {
                    TenantContext.set(oldConfig);
                }
            }
        }
        final Object tenantIdFinal = tenantId;

        // 2) build TenantConfig from targets
        List<TenantConfig.ConfigItem> items = new ArrayList<>();
        for (TenantTarget target : tenantTargets.value()) {
            if (target == null) {
                continue;
            }

            String[] tableNames = resolveTableNames(target);
            if (tableNames == null || tableNames.length == 0) {
                continue;
            }

            String columnName = resolveColumnName(target);
            if (columnName == null || columnName.trim().isEmpty()) {
                continue;
            }

            items.add(new TenantConfig.ConfigItem(
                    Arrays.asList(target.sqlTypes()),
                    Arrays.asList(tableNames),
                    columnName,
                    () -> tenantIdFinal,
                    () -> tenantIdFinal,
                    () -> tenantIdFinal,
                    () -> tenantIdFinal,
                    target.priority()
            ));
        }

        if (items.isEmpty()) {
            return pjp.proceed();
        }

        TenantConfig tenantConfig = new TenantConfig(items);

        try (TenantContextHolder.AutoCloseableHolder ignored = TenantContextHolder.setConfig(tenantConfig)) {
            return pjp.proceed();
        }
    }

    private Object resolveTenantId(TenantId tenantIdAnn) {
        Class<? extends TenantIdProvider> providerClass = tenantIdAnn.tenantIdProvider();
        if (providerClass != null && providerClass != TenantIdProvider.class) {
            TenantIdProvider provider = resolveProvider(providerClass);
            if (provider != null) {
                return provider.getTenantId();
            }
        }
        // fixed value
        String fixed = tenantIdAnn.value();
        if (fixed == null || fixed.trim().isEmpty()) {
            return null;
        }
        return fixed;
    }

    private String[] resolveTableNames(TenantTarget target) {
        Class<? extends TenantTableNamesProvider> providerClass = target.tableNamesProvider();
        if (providerClass != null && providerClass != TenantTableNamesProvider.class) {
            TenantTableNamesProvider provider = resolveProvider(providerClass);
            if (provider != null) {
                String[] result = provider.getTableNames();
                return result;
            }
        }
        return target.tableNames();
    }

    private String resolveColumnName(TenantTarget target) {
        Class<? extends TenantColumnNameProvider> providerClass = target.columnNameProvider();
        if (providerClass != null && providerClass != TenantColumnNameProvider.class) {
            TenantColumnNameProvider provider = resolveProvider(providerClass);
            if (provider != null) {
                return provider.getColumnName();
            }
        }
        return target.columnName();
    }

    private <T> T resolveProvider(Class<T> providerClass) {
        if (providerClass == null
                || providerClass == TenantIdProvider.class
                || providerClass == TenantTableNamesProvider.class
                || providerClass == TenantColumnNameProvider.class) {
            return null;
        }
        try {
            if (applicationContext != null) {
                return applicationContext.getBean(providerClass);
            }
        } catch (Exception ignored) {
            // ignore and fallback to newInstance
        }
        try {
            return providerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn("Tenant provider 实例化失败: {}", providerClass, e);
            return null;
        }
    }
}

