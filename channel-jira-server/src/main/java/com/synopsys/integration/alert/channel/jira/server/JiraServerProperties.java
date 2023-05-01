/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import java.util.Optional;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.jira.common.rest.JiraHttpClient;
import com.synopsys.integration.jira.common.server.configuration.JiraServerBasicAuthRestConfigBuilder;
import com.synopsys.integration.jira.common.server.configuration.JiraServerBearerAuthRestConfigBuilder;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JiraServerProperties {
    private final String url;
    private final JiraServerAuthorizationMethod authorizationMethod;
    private final String password;
    private final String username;
    private final String accessToken;
    private final boolean pluginCheckDisabled;
    private final ProxyInfo proxyInfo;

    public JiraServerProperties(
        String url,
        JiraServerAuthorizationMethod authorizationMethod,
        String password,
        String username,
        String accessToken,
        boolean pluginCheckDisabled,
        ProxyInfo proxyInfo
    ) {
        this.url = url;
        this.authorizationMethod = authorizationMethod;
        this.password = password;
        this.username = username;
        this.accessToken = accessToken;
        this.pluginCheckDisabled = pluginCheckDisabled;
        this.proxyInfo = proxyInfo;
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
            .setAuthPassword(password)
            .setAuthUsername(username)
            .setProxyInfo(proxyInfo);

        return builder.build();
    }

    public JiraServerRestConfig createJiraServerBearerAuthConfig() {
        JiraServerBearerAuthRestConfigBuilder builder = new JiraServerBearerAuthRestConfigBuilder();
        builder.setUrl(url)
            .setAccessToken(accessToken)
            .setProxyInfo(proxyInfo);

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
