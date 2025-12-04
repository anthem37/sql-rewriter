package io.github.anthem37.sql.rewriter.plugin.tenant.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.github.anthem37.sql.rewriter.core.util.GsonUtils;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 租户上下文管理器
 * <p>
 * 提供线程安全的租户配置存储和管理功能，确保每个线程的租户配置相互隔离。
 * 使用 TransmittableThreadLocal 确保在异步场景下也能正确传递租户配置。
 * </p>
 *
 * @author anthem37
 * @since 2025/12/04
 */
@Slf4j
public class TenantContext {

    /**
     * ThreadLocal存储，确保每个线程的租户配置相互隔离
     */
    private static final TransmittableThreadLocal<TenantConfig> HOLDER = new TransmittableThreadLocal<>();

    /**
     * 设置当前线程的租户配置
     *
     * <p>将租户配置存储到当前线程的ThreadLocal中。
     * 该配置只对当前线程可见，不会影响其他线程。
     *
     * @param config 租户配置对象，不能为null
     */
    public static void set(TenantConfig config) {
        TenantConfig oldConfig = HOLDER.get();
        HOLDER.set(config);

        if (Objects.isNull(oldConfig)) {
            log.debug("设置租户配置: newConfig={}", GsonUtils.toJson(config));
            return;
        }
        log.debug("更新租户配置: 从 oldConfig={} 更新为 newConfig={}", GsonUtils.toJson(oldConfig), GsonUtils.toJson(config));
    }

    /**
     * 获取当前线程的租户配置
     *
     * <p>从当前线程的ThreadLocal中获取租户配置。
     * 如果当前线程没有设置租户配置，则返回null。
     *
     * @return 当前线程的租户配置，如果没有设置则返回null
     */
    public static TenantConfig get() {
        return HOLDER.get();
    }

    /**
     * 移除当前线程的租户配置
     *
     * <p>清理当前线程的ThreadLocal中的租户配置，释放内存。
     * 必须在方法结束时调用，避免内存泄漏。
     *
     * <p>注意：通常由切面自动调用，无需手动调用。
     */
    public static void remove() {
        TenantConfig config = HOLDER.get();
        if (Objects.nonNull(config)) {
            log.debug("移除租户配置: config={}", GsonUtils.toJson(config));
        }
        HOLDER.remove();
    }

    /**
     * 检查当前线程是否设置了租户配置
     *
     * @return 如果当前线程有租户配置返回true，否则返回false
     */
    public static boolean hasConfig() {
        return HOLDER.get() != null;
    }
}