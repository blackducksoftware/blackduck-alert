/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class SettingsProxyModel extends ConfigWithMetadata implements Obfuscated<SettingsProxyModel> {
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private Boolean isProxyPasswordSet;
    private String proxyPassword;

    private List<String> nonProxyHosts;

    public SettingsProxyModel() {
        // For serialization
    }

    public SettingsProxyModel(String id, String name, String proxyHost, Integer proxyPort) {
        super(id, name);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public SettingsProxyModel(
        String id,
        String name,
        String createdAt,
        String lastUpdated,
        String proxyHost,
        Integer proxyPort,
        String proxyUsername,
        String proxyPassword,
        Boolean isProxyPasswordSet,
        List<String> nonProxyHosts
    ) {
        this(id, name, proxyHost, proxyPort);
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.isProxyPasswordSet = isProxyPasswordSet;
        this.nonProxyHosts = nonProxyHosts;
        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    @Override
    public SettingsProxyModel obfuscate() {
        return new SettingsProxyModel(
            getId(),
            getName(),
            getCreatedAt(),
            getLastUpdated(),
            proxyHost,
            proxyPort,
            proxyUsername,
            null,
            StringUtils.isNotBlank(proxyPassword),
            nonProxyHosts
        );
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public Optional<String> getProxyUsername() {
        return Optional.ofNullable(proxyUsername);
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public Optional<String> getProxyPassword() {
        return Optional.ofNullable(proxyPassword);
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Boolean getIsProxyPasswordSet() {
        return isProxyPasswordSet;
    }

    public void setIsProxyPasswordSet(Boolean isProxyPasswordSet) {
        this.isProxyPasswordSet = isProxyPasswordSet;
    }

    public Optional<List<String>> getNonProxyHosts() {
        return Optional.ofNullable(nonProxyHosts);
    }

    public void setNonProxyHosts(List<String> nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }
}
