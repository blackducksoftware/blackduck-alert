/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class JiraServerPropertiesFactory {
    private final JiraServerChannelKey channelKey;
    private final ProxyManager proxyManager;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public JiraServerPropertiesFactory(JiraServerChannelKey channelKey, ProxyManager proxyManager, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.channelKey = channelKey;
        this.proxyManager = proxyManager;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    public JiraServerProperties createJiraProperties(FieldUtility fieldUtility) {
        String url = fieldUtility.getStringOrNull(JiraServerDescriptor.KEY_SERVER_URL);
        String username = fieldUtility.getStringOrNull(JiraServerDescriptor.KEY_SERVER_USERNAME);
        String password = fieldUtility.getStringOrNull(JiraServerDescriptor.KEY_SERVER_PASSWORD);
        boolean pluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        ProxyInfo proxy = proxyManager.createProxyInfoForHost(url);
        return new JiraServerProperties(url, password, username, pluginCheckDisabled, proxy);
    }

    public JiraServerProperties createJiraProperties(FieldModel fieldModel) {
        String url = fieldModel.getFieldValue(JiraServerDescriptor.KEY_SERVER_URL).orElse("");
        String username = fieldModel.getFieldValue(JiraServerDescriptor.KEY_SERVER_USERNAME).orElse("");
        String password = fieldModel.getFieldValueModel(JiraServerDescriptor.KEY_SERVER_PASSWORD)
                              .map(this::getAppropriateAccessToken)
                              .orElse("");
        boolean pluginCheckDisabled = fieldModel.getFieldValue(JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK).map(Boolean::parseBoolean).orElse(false);

        ProxyInfo proxy = proxyManager.createProxyInfoForHost(url);
        return new JiraServerProperties(url, password, username, pluginCheckDisabled, proxy);
    }

    public JiraServerProperties createJiraProperties() throws AlertConfigurationException {
        ConfigurationModel jiraServerGlobalConfig = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(channelKey, ConfigContextEnum.GLOBAL)
                                                        .stream()
                                                        .findAny()
                                                        .orElseThrow(() -> new AlertConfigurationException("Missing Jira Server global configuration"));

        FieldUtility fieldUtility = new FieldUtility(jiraServerGlobalConfig.getCopyOfKeyToFieldMap());
        return createJiraProperties(fieldUtility);
    }

    private String getAppropriateAccessToken(FieldValueModel fieldAccessToken) {
        String accessToken = fieldAccessToken.getValue().orElse("");
        boolean accessTokenSet = fieldAccessToken.getIsSet();
        if (StringUtils.isBlank(accessToken) && accessTokenSet) {
            return configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(ChannelKeys.JIRA_SERVER, ConfigContextEnum.GLOBAL)
                       .stream()
                       .findFirst()
                       .flatMap(configurationModel -> configurationModel.getField(JiraServerDescriptor.KEY_SERVER_PASSWORD))
                       .flatMap(ConfigurationFieldModel::getFieldValue)
                       .orElse("");
        }
        return accessToken;
    }

}
