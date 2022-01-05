/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.jira.common.rest.JiraHttpClient;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfig;
import com.synopsys.integration.jira.common.server.configuration.JiraServerRestConfigBuilder;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class JiraServerProperties {
    private final String url;
    private final String password;
    private final String username;
    private final boolean pluginCheckDisabled;
    private final ProxyInfo proxyInfo;

    public JiraServerProperties(String url, String password, String username, boolean pluginCheckDisabled, ProxyInfo proxyInfo) {
        this.url = url;
        this.password = password;
        this.username = username;
        this.pluginCheckDisabled = pluginCheckDisabled;
        this.proxyInfo = proxyInfo;
    }

    public JiraServerRestConfig createJiraServerConfig() throws IssueTrackerException {
        JiraServerRestConfigBuilder jiraServerConfigBuilder = new JiraServerRestConfigBuilder();

        jiraServerConfigBuilder.setUrl(url);
        jiraServerConfigBuilder.setAuthPassword(password);
        jiraServerConfigBuilder.setAuthUsername(username);
        jiraServerConfigBuilder.setProxyInfo(proxyInfo);
        try {
            return jiraServerConfigBuilder.build();
        } catch (IllegalArgumentException e) {
            throw new IssueTrackerException("There was an issue building the configuration: " + e.getMessage());
        }
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

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isPluginCheckDisabled() {
        return pluginCheckDisabled;
    }

}
