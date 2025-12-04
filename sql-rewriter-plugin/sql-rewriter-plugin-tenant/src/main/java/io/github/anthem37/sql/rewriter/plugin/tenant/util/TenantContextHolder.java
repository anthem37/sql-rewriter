package io.github.anthem37.sql.rewriter.plugin.tenant.util;

import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 租户上下文管理器
 * <p>
 * 提供了安全的方式来管理租户配置的生命周期，确保ThreadLocal在异常情况下也能被正确清理。
 * 使用 try-with-resources 模式，自动管理租户配置的设置和清理。
 * </p>
 *
 * <pre>{@code
 * // 使用示例
 * TenantConfig config = new TenantConfig(...);
 * try (TenantContextHolder.AutoCloseableHolder holder = TenantContextHolder.setConfig(config)) {
 *     // 业务逻辑
 *     service.executeSql();
 * } // 自动清理
 * }</pre>
 *
 * @author anthem37
 * @since 2025/12/04
 */
@Slf4j
public class TenantContextHolder {

    /**
     * 设置租户配置并返回自动清理的持有者
     *
     * @param config 租户配置
     * @return 自动清理的持有者
     */
    public static AutoCloseableHolder setConfig(TenantConfig config) {
        TenantContext.set(config);
        return new AutoCloseableHolder();
    }

    /**
     * 在指定租户配置下执行任务
     *
     * @param config 租户配置
     * @param task   要执行的任务
     * @param <T>    任务返回值类型
     * @return 任务执行结果
     * @throws Exception 任务执行时可能抛出的异常
     */
    public static <T> T executeWithTenant(TenantConfig config, TenantTask<T> task) throws Exception {
        try (AutoCloseableHolder holder = setConfig(config)) {
            return task.execute();
        }
    }

    /**
     * 在指定租户配置下执行任务，无返回值
     *
     * @param config 租户配置
     * @param task   要执行的任务
     * @throws Exception 任务执行时可能抛出的异常
     */
    public static void executeWithTenant(TenantConfig config, TenantVoidTask task) throws Exception {
        try (AutoCloseableHolder holder = setConfig(config)) {
            task.execute();
        }
    }

    /**
     * 在指定租户配置下执行任务，捕获异常
     *
     * @param config 租户配置
     * @param task   要执行的任务
     * @param <T>    任务返回值类型
     * @return 任务执行结果，如果异常则返回null
     */
    public static <T> T executeWithTenantSafely(TenantConfig config, TenantTask<T> task) {
        try (AutoCloseableHolder holder = setConfig(config)) {
            return task.execute();
        } catch (Exception e) {
            // 记录异常但不传播
            log.error("执行租户任务时发生异常: ", e);
            return null;
        }
    }

    /**
     * 在指定租户配置下执行任务，捕获异常，无返回值
     *
     * @param config 租户配置
     * @param task   要执行的任务
     */
    public static void executeWithTenantSafely(TenantConfig config, TenantVoidTask task) {
        try (AutoCloseableHolder holder = setConfig(config)) {
            task.execute();
        } catch (Exception e) {
            // 记录异常但不传播
            log.error("执行租户任务时发生异常: ", e);
        }
    }

    /**
     * 有返回值的任务接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface TenantTask<T> {
        /**
         * 执行任务
         *
         * @return 任务结果
         * @throws Exception 执行时可能抛出的异常
         */
        T execute() throws Exception;
    }

    /**
     * 无返回值的任务接口
     */
    @FunctionalInterface
    public interface TenantVoidTask {
        /**
         * 执行任务
         *
         * @throws Exception 执行时可能抛出的异常
         */
        void execute() throws Exception;
    }

    /**
     * 自动清理的持有者，用于 try-with-resources 模式
     */
    public static class AutoCloseableHolder implements AutoCloseable {
        @Override
        public void close() {
            TenantContext.remove();
        }
    }
}