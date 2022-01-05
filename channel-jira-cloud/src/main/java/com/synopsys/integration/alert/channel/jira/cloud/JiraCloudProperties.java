/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfig;
import com.synopsys.integration.jira.common.cloud.configuration.JiraCloudRestConfigBuilder;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.rest.JiraHttpClient;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JiraCloudProperties {
    private final String url;
    private final String accessToken;
    private final String username;
    private final boolean pluginCheckDisabled;
    private final ProxyInfo proxyInfo;

    public static JiraCloudProperties fromConfig(ConfigurationModel configurationModel, ProxyInfo proxyInfo) {
        FieldUtility fieldUtility = new FieldUtility(configurationModel.getCopyOfKeyToFieldMap());
        String url = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_URL);
        String accessToken = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        String username = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        boolean pluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled, proxyInfo);
    }

    public JiraCloudProperties(String url, String accessToken, String username, boolean pluginCheckDisabled, ProxyInfo proxyInfo) {
        this.url = url;
        this.accessToken = accessToken;
        this.username = username;
        this.pluginCheckDisabled = pluginCheckDisabled;
        this.proxyInfo = proxyInfo;
    }

    public JiraCloudRestConfig createJiraServerConfig() throws IssueTrackerException {
        JiraCloudRestConfigBuilder jiraServerConfigBuilder = new JiraCloudRestConfigBuilder();

        jiraServerConfigBuilder.setUrl(url);
        jiraServerConfigBuilder.setApiToken(accessToken);
        jiraServerConfigBuilder.setAuthUserEmail(username);
        jiraServerConfigBuilder.setProxyInfo(proxyInfo);
        try {
            return jiraServerConfigBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new IssueTrackerException("There was an issue building the configuration: " + e.getMessage());
        }
    }

    public JiraCloudServiceFactory createJiraServicesCloudFactory(Logger logger, Gson gson) throws IssueTrackerException {
        JiraCloudRestConfig jiraServerConfig = createJiraServerConfig();
        Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        JiraHttpClient jiraHttpClient = jiraServerConfig.createJiraHttpClient(intLogger);
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

}
