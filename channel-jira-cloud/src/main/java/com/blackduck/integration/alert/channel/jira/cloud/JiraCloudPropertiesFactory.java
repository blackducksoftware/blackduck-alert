/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud;

import com.blackduck.integration.jira.common.cloud.configuration.JiraCloudRestConfigBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.certificates.AlertSSLContextManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.rest.proxy.ProxyInfo;

@Component
public class JiraCloudPropertiesFactory {
    private final JiraCloudChannelKey channelKey;
    private final ProxyManager proxyManager;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final AlertSSLContextManager alertSSLContextManager;

    @Autowired
    public JiraCloudPropertiesFactory(
        JiraCloudChannelKey channelKey,
        ProxyManager proxyManager,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        AlertSSLContextManager alertSSLContextManager
    ) {
        this.channelKey = channelKey;
        this.proxyManager = proxyManager;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.alertSSLContextManager = alertSSLContextManager;
    }

    public JiraCloudProperties createJiraProperties(FieldUtility fieldUtility) {
        String url = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_URL);
        String username = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        String accessToken = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        boolean pluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(url);

        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled, proxy, alertSSLContextManager.buildWithClientCertificate().orElse(null), JiraCloudRestConfigBuilder.DEFAULT_TIMEOUT_SECONDS);
    }

    public JiraCloudProperties createJiraProperties(FieldModel fieldModel) {
        String url = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_URL).orElse("");
        String username = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS).orElse("");
        String accessToken = fieldModel.getFieldValueModel(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN)
            .map(this::getAppropriateAccessToken)
            .orElse("");
        boolean pluginCheckDisabled = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK)
            .map(Boolean::parseBoolean)
            .orElse(false);

        ProxyInfo proxy = proxyManager.createProxyInfoForHost(url);
        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled, proxy, alertSSLContextManager.buildWithClientCertificate().orElse(null), JiraCloudRestConfigBuilder.DEFAULT_TIMEOUT_SECONDS);
    }

    public JiraCloudProperties createJiraProperties() throws AlertConfigurationException {
        ConfigurationModel jiraCloudGlobalConfig = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(channelKey, ConfigContextEnum.GLOBAL)
            .stream()
            .findAny()
            .orElseThrow(() -> new AlertConfigurationException("Missing Jira Cloud global configuration"));

        FieldUtility fieldUtility = new FieldUtility(jiraCloudGlobalConfig.getCopyOfKeyToFieldMap());
        return createJiraProperties(fieldUtility);
    }

    private String getAppropriateAccessToken(FieldValueModel fieldAccessToken) {
        String accessToken = fieldAccessToken.getValue().orElse("");
        boolean accessTokenSet = fieldAccessToken.getIsSet();
        if (StringUtils.isBlank(accessToken) && accessTokenSet) {
            return configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKeys.JIRA_CLOUD, ConfigContextEnum.GLOBAL)
                .stream()
                .findFirst()
                .flatMap(configurationModel -> configurationModel.getField(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN))
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .orElse("");
        }
        return accessToken;
    }

}
