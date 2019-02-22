package com.synopsys.integration.alert.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class MockConfigurationModelFactory {
    public static List<ConfigurationFieldModel> createHipChatDistributionFields() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel color = createFieldModel(HipChatDescriptor.KEY_COLOR, "RED");
        final ConfigurationFieldModel notify = createFieldModel(HipChatDescriptor.KEY_NOTIFY, "false");
        final ConfigurationFieldModel room = createFieldModel(HipChatDescriptor.KEY_ROOM_ID, "4056783");

        fields.add(color);
        fields.add(notify);
        fields.add(room);

        final Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("HipChat Test Job", HipChatChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createSlackDistributionFields() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel channel = createFieldModel(SlackDescriptor.KEY_CHANNEL_NAME, "Alert channel");
        final ConfigurationFieldModel username = createFieldModel(SlackDescriptor.KEY_CHANNEL_USERNAME, "Alert unit test");
        final ConfigurationFieldModel webhook = createFieldModel(SlackDescriptor.KEY_WEBHOOK, "Webhook");

        fields.add(channel);
        fields.add(username);
        fields.add(webhook);

        final Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("Slack Test Job", SlackChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createEmailDistributionFieldsProjectOwnerOnly() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        //        final ConfigurationFieldModel emailAddresses = createConfigurationFieldModel(EmailDescriptor.KEY_EMAIL_ADDRESSES, List.of("noreply@blackducksoftware.com"));
        final ConfigurationFieldModel projectOwnerOnly = createFieldModel(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, "true");
        final ConfigurationFieldModel subjectLine = createFieldModel(EmailDescriptor.KEY_SUBJECT_LINE, "Alert unit test subject line");

        //        fields.add(emailAddresses);
        fields.add(projectOwnerOnly);
        fields.add(subjectLine);

        final Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("Email Test Job", EmailChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createEmailDistributionFields() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel emailAddresses = createFieldModel(EmailDescriptor.KEY_EMAIL_ADDRESSES, List.of("noreply@blackducksoftware.com"));
        final ConfigurationFieldModel projectOwnerOnly = createFieldModel(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, "false");
        final ConfigurationFieldModel subjectLine = createFieldModel(EmailDescriptor.KEY_SUBJECT_LINE, "Alert unit test subject line");

        fields.add(emailAddresses);
        fields.add(projectOwnerOnly);
        fields.add(subjectLine);

        final Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("Email Test Job", EmailChannel.COMPONENT_NAME);
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createBlackDuckDistributionFields() {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel notificationTypes = createFieldModel(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, List.of(NotificationType.VULNERABILITY.toString(), NotificationType.RULE_VIOLATION.toString()));
        final ConfigurationFieldModel formatType = createFieldModel(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, FormatType.DEFAULT.toString());
        final ConfigurationFieldModel filterByProject = createFieldModel(CommonDistributionConfiguration.KEY_FILTER_BY_PROJECT, "true");
        final ConfigurationFieldModel projectNamePattern = createFieldModel(CommonDistributionConfiguration.KEY_PROJECT_NAME_PATTERN, ".*UnitTest.*");
        final ConfigurationFieldModel configuredProject = createFieldModel(CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT, List.of("TestProject1", "TestProject2"));

        fields.add(notificationTypes);
        fields.add(formatType);
        fields.add(filterByProject);
        fields.add(projectNamePattern);
        fields.add(configuredProject);

        return fields;
    }

    public static List<ConfigurationFieldModel> createCommonDistributionFields(final String jobName, final String channelDescriptorName) {
        final List<ConfigurationFieldModel> fields = new ArrayList<>();

        final ConfigurationFieldModel name = createFieldModel(ChannelDistributionUIConfig.KEY_NAME, jobName);
        final ConfigurationFieldModel channelName = createFieldModel(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, channelDescriptorName);
        final ConfigurationFieldModel providerName = createFieldModel(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, BlackDuckProvider.COMPONENT_NAME);
        final ConfigurationFieldModel frequencyType = createFieldModel(ChannelDistributionUIConfig.KEY_FREQUENCY, FrequencyType.REAL_TIME.toString());

        fields.add(name);
        fields.add(channelName);
        fields.add(providerName);
        fields.add(frequencyType);

        return fields;
    }

    public static ConfigurationModel createCommonConfigModel(final Long id, final Long descriptorId, final String distributionType, final String name, final String providerName, final String frequency,
        final String filterByProject, final String projectNamePattern, final List<String> configuredProjects, final List<String> notificationTypes, final String formatType) {
        final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);

        Mockito.when(configurationModel.getConfigurationId()).thenReturn(id);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(descriptorId);

        final List<ConfigurationFieldModel> fieldList = new ArrayList<>();
        mockField(fieldList, configurationModel, ChannelDistributionUIConfig.KEY_NAME, name);
        mockField(fieldList, configurationModel, ChannelDistributionUIConfig.KEY_FREQUENCY, frequency);
        mockField(fieldList, configurationModel, ChannelDistributionUIConfig.KEY_PROVIDER_NAME, providerName);
        mockField(fieldList, configurationModel, ChannelDistributionUIConfig.KEY_CHANNEL_NAME, distributionType);

        mockField(fieldList, configurationModel, ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationTypes);
        mockField(fieldList, configurationModel, ProviderDistributionUIConfig.KEY_FORMAT_TYPE, formatType);

        mockField(fieldList, configurationModel, CommonDistributionConfiguration.KEY_FILTER_BY_PROJECT, filterByProject);
        mockField(fieldList, configurationModel, CommonDistributionConfiguration.KEY_PROJECT_NAME_PATTERN, projectNamePattern);
        mockField(fieldList, configurationModel, CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT, configuredProjects);

        Mockito.when(configurationModel.getConfigurationId()).thenReturn(id);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(descriptorId);
        Mockito.when(configurationModel.getCopyOfFieldList()).thenReturn(fieldList);
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fieldList));

        return configurationModel;
    }

    public static ConfigurationJobModel createCommonJobConfigModel(final UUID uuid, final Long id, final Long descriptorId, final String distributionType, final String name, final String providerName, final String frequency,
        final String filterByProject, final String projectNamePattern, final List<String> configuredProjects, final List<String> notificationTypes, final String formatType) {
        final ConfigurationJobModel configurationJobModel = Mockito.mock(ConfigurationJobModel.class);
        Mockito.when(configurationJobModel.getJobId()).thenReturn(uuid);

        final ConfigurationModel configurationModel = createCommonConfigModel(id, descriptorId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, configuredProjects, notificationTypes, formatType);
        final Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapFieldKeyToFields(configurationModel.getCopyOfFieldList());
        Mockito.when(configurationJobModel.createKeyToFieldMap()).thenReturn(fieldModelMap);

        return configurationJobModel;
    }

    private static void mockField(final List<ConfigurationFieldModel> fieldList, final ConfigurationModel configurationModel, final String key, final String value) {
        mockField(fieldList, configurationModel, key, List.of(value));
    }

    private static void mockField(final List<ConfigurationFieldModel> fieldList, final ConfigurationModel configurationModel, final String key, final Collection<String> values) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValues(values);
        Mockito.when(configurationModel.getField(key)).thenReturn(Optional.of(field));
        fieldList.add(field);
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
