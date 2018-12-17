package com.synopsys.integration.alert.mock;

import java.util.Collection;

import com.sun.tools.javac.util.List;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDistributionUIConfig;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDistributionUIConfig;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDistributionUIConfig;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class MockConfigurationModelFactory {

    public static Collection<ConfigurationFieldModel> createHipChatConfigurationFields() {
        final ConfigurationFieldModel color = createFieldModel(HipChatDistributionUIConfig.KEY_COLOR, "RED");
        final ConfigurationFieldModel notify = createFieldModel(HipChatDistributionUIConfig.KEY_NOTIFY, "false");
        final ConfigurationFieldModel room = createFieldModel(HipChatDistributionUIConfig.KEY_ROOM_ID, "4056783");

        final List<ConfigurationFieldModel> fields = List.of(color, notify, room);
        final Collection<ConfigurationFieldModel> commonFields = createCommonBlackDuckConfigurationFields("HipChat Test Job", HipChatChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static Collection<ConfigurationFieldModel> createSlackConfigurationFields() {
        final ConfigurationFieldModel channel = createFieldModel(SlackUIConfig.KEY_CHANNEL_NAME, "Alert channel");
        final ConfigurationFieldModel username = createFieldModel(SlackUIConfig.KEY_CHANNEL_USERNAME, "Alert unit test");
        final ConfigurationFieldModel webhook = createFieldModel(SlackUIConfig.KEY_WEBHOOK, "Webhook");

        final List<ConfigurationFieldModel> fields = List.of(channel, username, webhook);
        final Collection<ConfigurationFieldModel> commonFields = createCommonBlackDuckConfigurationFields("Slack Test Job", SlackChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static Collection<ConfigurationFieldModel> createEmailConfigurationFields() {
        final ConfigurationFieldModel emailAddresses = createFieldModel(EmailDistributionUIConfig.KEY_EMAIL_ADDRESSES, List.of("noreply@blackducksoftware.com"));
        final ConfigurationFieldModel projectOwnerOnly = createFieldModel(EmailDistributionUIConfig.KEY_PROJECT_OWNER_ONLY, "true");
        final ConfigurationFieldModel subjectLine = createFieldModel(EmailDistributionUIConfig.KEY_SUBJECT_LINE, "Alert unit test subject line");

        final List<ConfigurationFieldModel> fields = List.of(emailAddresses, projectOwnerOnly, subjectLine);
        final Collection<ConfigurationFieldModel> commonFields = createCommonBlackDuckConfigurationFields("Email Test Job", EmailGroupChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static Collection<ConfigurationFieldModel> createCommonBlackDuckConfigurationFields(final String jobName, final String channelDescriptorName) {
        final ConfigurationFieldModel name = createFieldModel(CommonDistributionUIConfig.KEY_NAME, jobName);
        final ConfigurationFieldModel channelName = createFieldModel(CommonDistributionUIConfig.KEY_CHANNEL_NAME, channelDescriptorName);
        final ConfigurationFieldModel providerName = createFieldModel(CommonDistributionUIConfig.KEY_PROVIDER_NAME, BlackDuckProvider.COMPONENT_NAME);
        final ConfigurationFieldModel notificationTypes = createFieldModel(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, List.of(NotificationType.VULNERABILITY.toString(), NotificationType.RULE_VIOLATION.toString()));
        final ConfigurationFieldModel frequencyType = createFieldModel(CommonDistributionUIConfig.KEY_FREQUENCY, FrequencyType.REAL_TIME.toString());
        final ConfigurationFieldModel formatType = createFieldModel(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.DEFAULT.toString());
        final ConfigurationFieldModel filterByProject = createFieldModel(BlackDuckDistributionUIConfig.KEY_FILTER_BY_PROJECT, "true");
        final ConfigurationFieldModel projectNamePattern = createFieldModel(BlackDuckDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, ".*UnitTest.*");
        final ConfigurationFieldModel configuredProject = createFieldModel(BlackDuckDistributionUIConfig.KEY_CONFIGURED_PROJECT, List.of("TestProject1", "TestProject2"));

        return List.of(name, channelName, providerName, notificationTypes, frequencyType, formatType, filterByProject, projectNamePattern, configuredProject);
    }

    public static ConfigurationFieldModel createFieldModel(final String fieldKey, final Collection<String> fieldValues) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
        configurationFieldModel.setFieldValues(fieldValues);
        return configurationFieldModel;
    }

    public static ConfigurationFieldModel createSensitiveFieldModel(final String fieldKey, final Collection<String> fieldValues) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.createSensitive(fieldKey);
        configurationFieldModel.setFieldValues(fieldValues);
        return configurationFieldModel;
    }

    public static ConfigurationFieldModel createFieldModel(final String fieldKey, final String fieldValue) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
        configurationFieldModel.setFieldValue(fieldValue);
        return configurationFieldModel;
    }

    public static ConfigurationFieldModel createSensitiveFieldModel(final String fieldKey, final String fieldValue) {
        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.createSensitive(fieldKey);
        configurationFieldModel.setFieldValue(fieldValue);
        return configurationFieldModel;
    }
}
