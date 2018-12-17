package com.synopsys.integration.alert.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel color = createFieldModel(HipChatDistributionUIConfig.KEY_COLOR, "RED");
        final ConfigurationFieldModel notify = createFieldModel(HipChatDistributionUIConfig.KEY_NOTIFY, "false");
        final ConfigurationFieldModel room = createFieldModel(HipChatDistributionUIConfig.KEY_ROOM_ID, "4056783");

        fields.add(color);
        fields.add(notify);
        fields.add(room);

        final Collection<ConfigurationFieldModel> commonFields = createCommonBlackDuckConfigurationFields("HipChat Test Job", HipChatChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static Collection<ConfigurationFieldModel> createSlackConfigurationFields() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel channel = createFieldModel(SlackUIConfig.KEY_CHANNEL_NAME, "Alert channel");
        final ConfigurationFieldModel username = createFieldModel(SlackUIConfig.KEY_CHANNEL_USERNAME, "Alert unit test");
        final ConfigurationFieldModel webhook = createFieldModel(SlackUIConfig.KEY_WEBHOOK, "Webhook");

        fields.add(channel);
        fields.add(username);
        fields.add(webhook);

        final Collection<ConfigurationFieldModel> commonFields = createCommonBlackDuckConfigurationFields("Slack Test Job", SlackChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static Collection<ConfigurationFieldModel> createEmailConfigurationFields() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel emailAddresses = createFieldModel(EmailDistributionUIConfig.KEY_EMAIL_ADDRESSES, List.of("noreply@blackducksoftware.com"));
        final ConfigurationFieldModel projectOwnerOnly = createFieldModel(EmailDistributionUIConfig.KEY_PROJECT_OWNER_ONLY, "true");
        final ConfigurationFieldModel subjectLine = createFieldModel(EmailDistributionUIConfig.KEY_SUBJECT_LINE, "Alert unit test subject line");

        fields.add(emailAddresses);
        fields.add(projectOwnerOnly);
        fields.add(subjectLine);

        final Collection<ConfigurationFieldModel> commonFields = createCommonBlackDuckConfigurationFields("Email Test Job", EmailGroupChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static Collection<ConfigurationFieldModel> createCommonBlackDuckConfigurationFields(final String jobName, final String channelDescriptorName) {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel name = createFieldModel(CommonDistributionUIConfig.KEY_NAME, jobName);
        final ConfigurationFieldModel channelName = createFieldModel(CommonDistributionUIConfig.KEY_CHANNEL_NAME, channelDescriptorName);
        final ConfigurationFieldModel providerName = createFieldModel(CommonDistributionUIConfig.KEY_PROVIDER_NAME, BlackDuckProvider.COMPONENT_NAME);
        final ConfigurationFieldModel notificationTypes = createFieldModel(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, List.of(NotificationType.VULNERABILITY.toString(), NotificationType.RULE_VIOLATION.toString()));
        final ConfigurationFieldModel frequencyType = createFieldModel(CommonDistributionUIConfig.KEY_FREQUENCY, FrequencyType.REAL_TIME.toString());
        final ConfigurationFieldModel formatType = createFieldModel(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.DEFAULT.toString());
        final ConfigurationFieldModel filterByProject = createFieldModel(BlackDuckDistributionUIConfig.KEY_FILTER_BY_PROJECT, "true");
        final ConfigurationFieldModel projectNamePattern = createFieldModel(BlackDuckDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, ".*UnitTest.*");
        final ConfigurationFieldModel configuredProject = createFieldModel(BlackDuckDistributionUIConfig.KEY_CONFIGURED_PROJECT, List.of("TestProject1", "TestProject2"));

        fields.add(name);
        fields.add(channelName);
        fields.add(providerName);
        fields.add(notificationTypes);
        fields.add(frequencyType);
        fields.add(formatType);
        fields.add(filterByProject);
        fields.add(projectNamePattern);
        fields.add(configuredProject);

        return fields;
    }

    public static Map<String, ConfigurationFieldModel> mapFieldKeyToFields(final Collection<ConfigurationFieldModel> fields) {
        return fields
                   .stream()
                   .collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));
    }

    public static Map<String, ConfigurationFieldModel> mapStringsToFields(final Map<String, String> fields) {
        final Map<String, ConfigurationFieldModel> configurationFieldMap = new HashMap<>(fields.size());
        for (final Map.Entry<String, String> entry : fields.entrySet()) {
            final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(entry.getKey());
            configurationFieldModel.setFieldValue(entry.getValue());
            configurationFieldMap.put(entry.getKey(), configurationFieldModel);
        }

        return configurationFieldMap;
    }

    public static Map<String, ConfigurationFieldModel> mapStringsToSensitiveFields(final Map<String, String> fields) {
        final Map<String, ConfigurationFieldModel> configurationFieldMap = new HashMap<>(fields.size());
        for (final Map.Entry<String, String> entry : fields.entrySet()) {
            final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.createSensitive(entry.getKey());
            configurationFieldModel.setFieldValue(entry.getValue());
            configurationFieldMap.put(entry.getKey(), configurationFieldModel);
        }

        return configurationFieldMap;
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
