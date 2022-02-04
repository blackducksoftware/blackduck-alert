/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;

public class JiraServerGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<JiraServerGlobalConfigModel> {
    private String url;
    private String userName;
    private Boolean isPasswordSet;
    private String password;
    private Boolean disablePluginCheck;

    public JiraServerGlobalConfigModel() {
        // For serialization
    }

    public JiraServerGlobalConfigModel(String id, String name, String url, String userName, String password) {
        super(id, name);
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    public JiraServerGlobalConfigModel(String id, String name, String createdAt, String lastUpdated, String url, String userName, String password, Boolean isPasswordSet, Boolean disablePluginCheck) {
        this(id, name, url, userName, password);
        this.isPasswordSet = isPasswordSet;
        this.disablePluginCheck = disablePluginCheck;
        setCreatedAt(createdAt);
        setLastUpdated(lastUpdated);
    }

    @Override
    public JiraServerGlobalConfigModel obfuscate() {
        return new JiraServerGlobalConfigModel(
            getId(),
            getName(),
            getCreatedAt(),
            getLastUpdated(),
            url,
            userName,
            null,
            StringUtils.isNotBlank(password),
            disablePluginCheck
        );
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public Optional<Boolean> getIsPasswordSet() {
        return Optional.ofNullable(isPasswordSet);
    }

    public void setIsPasswordSet(Boolean passwordSet) {
        isPasswordSet = passwordSet;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<Boolean> getDisablePluginCheck() {
        return Optional.ofNullable(disablePluginCheck);
    }

    public void setDisablePluginCheck(Boolean disablePluginCheck) {
        this.disablePluginCheck = disablePluginCheck;
    }
}
