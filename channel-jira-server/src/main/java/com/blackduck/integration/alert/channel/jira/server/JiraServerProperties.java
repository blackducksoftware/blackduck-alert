/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;

import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.blackduck.integration.jira.common.rest.JiraHttpClient;
import com.blackduck.integration.jira.common.server.configuration.JiraServerBasicAuthRestConfigBuilder;
import com.blackduck.integration.jira.common.server.configuration.JiraServerBearerAuthRestConfigBuilder;
import com.blackduck.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

public class JiraServerProperties {
    private final String url;
    private final Integer timeout;
    private final JiraServerAuthorizationMethod authorizationMethod;
    private final String password;
    private final String username;
    private final String accessToken;
    private final boolean pluginCheckDisabled;
    private final ProxyInfo proxyInfo;
    private final SSLContext sslContext;

    public JiraServerProperties(
        String url,
        Integer timeout,
        JiraServerAuthorizationMethod authorizationMethod,
        String password,
        String username,
        String accessToken,
        boolean pluginCheckDisabled,
        ProxyInfo proxyInfo,
        @Nullable SSLContext sslContext
    ) {
        this.url = url;
        this.timeout = timeout;
        this.authorizationMethod = authorizationMethod;
        this.password = password;
        this.username = username;
        this.accessToken = accessToken;
        this.pluginCheckDisabled = pluginCheckDisabled;
        this.proxyInfo = proxyInfo;
        this.sslContext = sslContext;
    }

    public JiraServerRestConfig createJiraServerConfig() throws IssueTrackerException {
        try {
            return setAuthorizationConfig();
        } catch (IllegalArgumentException e) {
            throw new IssueTrackerException("There was an issue building the configuration: " + e.getMessage());
        }
    }

    private JiraServerRestConfig setAuthorizationConfig() {
        if (authorizationMethod == null) {
            return createJiraServerBasicAuthConfig();
        }

        switch (authorizationMethod) {
            case PERSONAL_ACCESS_TOKEN:
                return createJiraServerBearerAuthConfig();
            case BASIC:
            default:
                return createJiraServerBasicAuthConfig();
        }
    }

    public JiraServerRestConfig createJiraServerBasicAuthConfig() {
        JiraServerBasicAuthRestConfigBuilder builder = new JiraServerBasicAuthRestConfigBuilder();
        builder.setUrl(url)
            .setTimeoutInSeconds(timeout)
            .setAuthPassword(password)
            .setAuthUsername(username)
            .setProxyInfo(proxyInfo);
        if (sslContext != null) {
            builder.setSslContext(sslContext);
        }

        return builder.build();
    }

    public JiraServerRestConfig createJiraServerBearerAuthConfig() {
        JiraServerBearerAuthRestConfigBuilder builder = new JiraServerBearerAuthRestConfigBuilder();
        builder.setUrl(url)
            .setTimeoutInSeconds(timeout)
            .setAccessToken(accessToken)
            .setProxyInfo(proxyInfo);
        if (sslContext != null) {
            builder.setSslContext(sslContext);
        }

        return builder.build();
    }

    public JiraServerServiceFactory createJiraServicesServerFactory(Logger logger, Gson gson) throws IssueTrackerException {
        JiraServerRestConfig jiraServerConfig = createJiraServerConfig();
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        JiraHttpClient jiraHttpClient = jiraServerConfig.createJiraHttpClient(intLogger);
        return new JiraServerServiceFactory(intLogger, jiraHttpClient, gson);
    }

    public String getUrl() {
        return url;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public boolean isPluginCheckDisabled() {
        return pluginCheckDisabled;
    }

}
