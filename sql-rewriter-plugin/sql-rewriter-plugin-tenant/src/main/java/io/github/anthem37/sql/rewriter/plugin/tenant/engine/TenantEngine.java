package io.github.anthem37.sql.rewriter.plugin.tenant.engine;

import io.github.anthem37.sql.rewriter.core.engine.impl.SQLRewriteEngine;
import io.github.anthem37.sql.rewriter.plugin.tenant.config.TenantConfig;
import io.github.anthem37.sql.rewriter.plugin.tenant.util.TenantUtils;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户引擎
 * <p>
 * 用于处理租户相关的SQL重写逻辑。
 * </p>
 *
 * @author anthem37
 * @since 2025/11/19 20:10:04
 */
@Getter
public class TenantEngine extends SQLRewriteEngine {

    private final List<TenantConfig> tenantConfigs;

    public TenantEngine(List<TenantConfig> tenantConfigs) {
        super(tenantConfigs == null ? Collections.emptyList() : tenantConfigs.stream().map(TenantUtils::convert2TenantRule).collect(Collectors.toList()));
        this.tenantConfigs = Collections.unmodifiableList(tenantConfigs == null ? Collections.emptyList() : tenantConfigs);
    }


}
