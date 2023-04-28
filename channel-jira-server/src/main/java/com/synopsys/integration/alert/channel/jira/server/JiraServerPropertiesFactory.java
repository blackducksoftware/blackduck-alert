/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;

@Component
public class JiraServerPropertiesFactory {
    private final ProxyManager proxyManager;
    private final JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;
    private final JobAccessor jobAccessor;

    @Autowired
    public JiraServerPropertiesFactory(ProxyManager proxyManager, JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor, JobAccessor jobAccessor) {
        this.proxyManager = proxyManager;
        this.jiraServerGlobalConfigAccessor = jiraServerGlobalConfigAccessor;
        this.jobAccessor = jobAccessor;
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
            jiraServerGlobalConfigModel.getAuthorizationMethod(),
            jiraServerGlobalConfigModel.getPassword().orElse(null),
            jiraServerGlobalConfigModel.getUserName().orElse(null),
            jiraServerGlobalConfigModel.getAccessToken().orElse(null),
            jiraServerGlobalConfigModel.getDisablePluginCheck().orElse(false)
        );
    }

    public JiraServerProperties createJiraProperties(
        String url,
        JiraServerAuthorizationMethod authorizationMethod,
        String password,
        String username,
        String accessToken,
        boolean pluginCheckDisabled
    ) {
        return new JiraServerProperties(url, authorizationMethod, password, username, accessToken, pluginCheckDisabled, proxyManager.createProxyInfoForHost(url));
    }
}
