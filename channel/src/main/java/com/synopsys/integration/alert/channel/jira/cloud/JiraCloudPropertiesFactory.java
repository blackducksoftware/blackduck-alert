package com.synopsys.integration.alert.channel.jira.cloud;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class JiraCloudPropertiesFactory {
    private final Logger logger = LoggerFactory.getLogger(JiraCloudPropertiesFactory.class);

    private final JiraCloudChannelKey jiraChannelKey;
    private final ProxyManager proxyManager;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public JiraCloudPropertiesFactory(JiraCloudChannelKey jiraChannelKey, ProxyManager proxyManager, ConfigurationAccessor configurationAccessor) {
        this.jiraChannelKey = jiraChannelKey;
        this.proxyManager = proxyManager;
        this.configurationAccessor = configurationAccessor;
    }

    public JiraCloudProperties createJiraProperties(FieldUtility fieldUtility) {
        String url = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_URL);
        String username = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS);
        String accessToken = fieldUtility.getStringOrNull(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN);
        boolean pluginCheckDisabled = fieldUtility.getBooleanOrFalse(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK);
        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled, proxyManager.createProxyInfo());
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

        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled, proxyManager.createProxyInfo());
    }

    private String getAppropriateAccessToken(FieldValueModel fieldAccessToken) {
        String accessToken = fieldAccessToken.getValue().orElse("");
        boolean accessTokenSet = fieldAccessToken.getIsSet();
        if (StringUtils.isBlank(accessToken) && accessTokenSet) {
            try {
                return configurationAccessor.getConfigurationsByDescriptorKeyAndContext(jiraChannelKey, ConfigContextEnum.GLOBAL)
                           .stream()
                           .findFirst()
                           .flatMap(configurationModel -> configurationModel.getField(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN))
                           .flatMap(ConfigurationFieldModel::getFieldValue)
                           .orElse("");

            } catch (AlertDatabaseConstraintException e) {
                logger.error("Unable to retrieve existing Jira configuration.");
            }
        }
        return accessToken;
    }
}
