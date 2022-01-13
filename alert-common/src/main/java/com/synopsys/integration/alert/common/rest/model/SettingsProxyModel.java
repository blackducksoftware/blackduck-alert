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

public class SettingsProxyModel extends ConfigWithMetadata implements Obfuscated<SettingsProxyModel> {
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private Boolean isProxyPasswordSet;
    private String proxyPassword;

    private List<String> nonProxyHosts;

    public Optional<String> getProxyHost() {
        return Optional.ofNullable(proxyHost);
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Optional<Integer> getProxyPort() {
        return Optional.ofNullable(proxyPort);
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
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

    public Boolean getIsProxyPasswordSet() {
        return isProxyPasswordSet;
    }

    public void setIsProxyPasswordSet(Boolean isProxyPasswordSet) {
        this.isProxyPasswordSet = isProxyPasswordSet;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Optional<List<String>> getNonProxyHosts() {
        return Optional.ofNullable(nonProxyHosts);
    }

    public void setNonProxyHosts(List<String> nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    @Override
    public SettingsProxyModel obfuscate() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();

        settingsProxyModel.setId(getId());
        settingsProxyModel.setName(getName());
        settingsProxyModel.setLastUpdated(getLastUpdated());
        settingsProxyModel.setCreatedAt(getCreatedAt());

        settingsProxyModel.setProxyHost(proxyHost);
        settingsProxyModel.setProxyPort(proxyPort);
        settingsProxyModel.setProxyUsername(proxyUsername);
        settingsProxyModel.setNonProxyHosts(nonProxyHosts);

        settingsProxyModel.setIsProxyPasswordSet(StringUtils.isNotBlank(proxyPassword));
        settingsProxyModel.setProxyPassword(null);

        return settingsProxyModel;
    }
}
