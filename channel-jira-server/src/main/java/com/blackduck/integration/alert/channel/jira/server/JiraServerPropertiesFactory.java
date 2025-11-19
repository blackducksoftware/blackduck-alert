/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.certificates.AlertSSLContextManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;

@Component
public class JiraServerPropertiesFactory {
    public static final Integer DEFAULT_JIRA_TIMEOUT_SECONDS = 300;

    private final ProxyManager proxyManager;
    private final JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;
    private final JobAccessor jobAccessor;
    private final AlertSSLContextManager alertSSLContextManager;

    @Autowired
    public JiraServerPropertiesFactory(
        ProxyManager proxyManager,
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor,
        JobAccessor jobAccessor,
        AlertSSLContextManager alertSSLContextManager
    ) {
        this.proxyManager = proxyManager;
        this.jiraServerGlobalConfigAccessor = jiraServerGlobalConfigAccessor;
        this.jobAccessor = jobAccessor;
        this.alertSSLContextManager = alertSSLContextManager;
    }

    public JiraServerProperties createJiraProperties(UUID jiraServerConfigId) throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfig = jiraServerGlobalConfigAccessor.getConfiguration(jiraServerConfigId)
            .orElseThrow(() -> new AlertConfigurationException("Missing Jira Server global configuration"));

        return createJiraProperties(jiraServerGlobalConfig);
    }

    public JiraServerProperties createJiraPropertiesWithJobId(UUID jiraServerJobId) throws AlertConfigurationException {
        DistributionJobModel jiraServerDistributionJobConfiguration = jobAccessor.getJobById(jiraServerJobId)
            .orElseThrow(() -> new AlertConfigurationException("Missing Jira Server distribution configuration"));
        return createJiraProperties(jiraServerDistributionJobConfiguration.getChannelGlobalConfigId());
    }

    public JiraServerProperties createJiraProperties(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        return createJiraProperties(
            jiraServerGlobalConfigModel.getUrl(),
            jiraServerGlobalConfigModel.getTimeout().orElse(DEFAULT_JIRA_TIMEOUT_SECONDS),
            jiraServerGlobalConfigModel.getAuthorizationMethod(),
            jiraServerGlobalConfigModel.getPassword().orElse(null),
            jiraServerGlobalConfigModel.getUserName().orElse(null),
            jiraServerGlobalConfigModel.getAccessToken().orElse(null),
            jiraServerGlobalConfigModel.getDisablePluginCheck().orElse(false)
        );
    }

    public JiraServerProperties createJiraProperties(
        String url,
        Integer timeout,
        JiraServerAuthorizationMethod authorizationMethod,
        String password,
        String username,
        String accessToken,
        boolean pluginCheckDisabled
    ) {
        return new JiraServerProperties(
            url,
            timeout,
            authorizationMethod,
            password,
            username,
            accessToken,
            pluginCheckDisabled,
            proxyManager.createProxyInfoForHost(url),
            alertSSLContextManager.buildWithClientCertificate().orElse(null)
        );
    }
}
