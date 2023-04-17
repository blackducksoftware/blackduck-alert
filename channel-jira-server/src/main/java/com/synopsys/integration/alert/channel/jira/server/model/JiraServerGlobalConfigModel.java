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

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public class JiraServerGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<JiraServerGlobalConfigModel> {
    private String url;
    private JiraServerAuthorizationMethod authorizationMethod;
    private String userName;
    private Boolean isPasswordSet;
    private String password;
    private String accessToken;
    private Boolean isAccessTokenSet;
    private Boolean disablePluginCheck;

    public JiraServerGlobalConfigModel() {
        // For serialization
    }

    public JiraServerGlobalConfigModel(String id, String name, String url, JiraServerAuthorizationMethod authorizationMethod) {
        super(id, name);
        this.url = url;
        this.authorizationMethod = authorizationMethod;
    }

    public JiraServerGlobalConfigModel(
        String id,
        String name,
        String createdAt,
        String lastUpdated,
        String url,
        JiraServerAuthorizationMethod authorizationMethod,
        String userName,
        String password,
        Boolean isPasswordSet,
        String accessToken,
        Boolean isAccessTokenSet,
        Boolean disablePluginCheck
    ) {
        this(id, name, url, authorizationMethod);
        this.authorizationMethod = authorizationMethod;
        this.userName = userName;
        this.password = password;
        this.isPasswordSet = isPasswordSet;
        this.accessToken = accessToken;
        this.isAccessTokenSet = isAccessTokenSet;
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
            authorizationMethod,
            userName,
            null,
            StringUtils.isNotBlank(password),
            null,
            StringUtils.isNotBlank(accessToken),
            disablePluginCheck
        );
    }

    public String getUrl() {
        return url;
    }

    public JiraServerAuthorizationMethod getAuthorizationMethod() {
        return authorizationMethod;
    }

    public void setAuthorizationMethod(JiraServerAuthorizationMethod authorizationMethod) {
        this.authorizationMethod = authorizationMethod;
    }

    public Optional<String> getUserName() {
        return Optional.ofNullable(userName);
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Optional<Boolean> getIsAccessTokenSet() {
        return Optional.ofNullable(isAccessTokenSet);
    }

    public void setIsAccessTokenSet(Boolean accessTokenSet) {
        isAccessTokenSet = accessTokenSet;
    }

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Optional<Boolean> getDisablePluginCheck() {
        return Optional.ofNullable(disablePluginCheck);
    }

    public void setDisablePluginCheck(Boolean disablePluginCheck) {
        this.disablePluginCheck = disablePluginCheck;
    }
}
