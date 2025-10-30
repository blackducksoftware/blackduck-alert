/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

import org.slf4j.Logger;

import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.jira.common.cloud.configuration.JiraCloudRestConfig;
import com.blackduck.integration.jira.common.cloud.configuration.JiraCloudRestConfigBuilder;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.rest.JiraHttpClient;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;

public class JiraCloudProperties {
    private final String url;
    private final String accessToken;
    private final String username;
    private final int timeoutInSeconds;
    private final boolean pluginCheckDisabled;
    private final ProxyInfo proxyInfo;
    private final SSLContext sslContext;

    public static JiraCloudProperties fromConfig(ConfigurationModel configurationModel, ProxyInfo proxyInfo, @Nullable SSLContext sslContext) {
        FieldUtility fieldUtility = new FieldUtility(configurationModel.getCopyOfKeyToFieldMap());
        String url = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_URL);
        String accessToken = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        String username = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        boolean pluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        int timeoutInSeconds = fieldUtility.getString(JiraCloudDescriptor.KEY_JIRA_TIMEOUT)
                .map(Integer::parseInt)
                .orElse(300);
        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled, proxyInfo, sslContext, timeoutInSeconds);
    }

    public JiraCloudProperties(String url, String accessToken, String username, boolean pluginCheckDisabled, ProxyInfo proxyInfo, @Nullable SSLContext sslContext, int timeoutInSeconds) {
        this.url = url;
        this.accessToken = accessToken;
        this.username = username;
        this.pluginCheckDisabled = pluginCheckDisabled;
        this.proxyInfo = proxyInfo;
        this.sslContext = sslContext;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public JiraCloudRestConfig createJiraCloudConfig() throws IssueTrackerException {
        JiraCloudRestConfigBuilder jiraCloudConfigBuilder = new JiraCloudRestConfigBuilder();

        jiraCloudConfigBuilder.setUrl(url);
        jiraCloudConfigBuilder.setApiToken(accessToken);
        jiraCloudConfigBuilder.setAuthUserEmail(username);
        jiraCloudConfigBuilder.setProxyInfo(proxyInfo);
        jiraCloudConfigBuilder.setTimeoutInSeconds(timeoutInSeconds);
        if (sslContext != null) {
            jiraCloudConfigBuilder.setSslContext(sslContext);
        }
        try {
            return jiraCloudConfigBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new IssueTrackerException("There was an issue building the configuration: " + e.getMessage());
        }
    }

    public JiraCloudServiceFactory createJiraServicesCloudFactory(Logger logger, Gson gson) throws IssueTrackerException {
        JiraCloudRestConfig jiraCloudConfig = createJiraCloudConfig();
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        JiraHttpClient jiraHttpClient = jiraCloudConfig.createJiraHttpClient(intLogger);
        return new JiraCloudServiceFactory(intLogger, jiraHttpClient, gson);
    }

    public String getUrl() {
        return url;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUsername() {
        return username;
    }

    public boolean isPluginCheckDisabled() {
        return pluginCheckDisabled;
    }

    public int getTimeoutInSeconds() { return timeoutInSeconds; }

}
