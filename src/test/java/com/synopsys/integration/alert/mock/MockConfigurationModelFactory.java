package com.synopsys.integration.alert.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class MockConfigurationModelFactory {
    public static List<ConfigurationFieldModel> createSlackDistributionFields() {
        List<ConfigurationFieldModel> fields = new ArrayList<>();

        ConfigurationFieldModel channel = createFieldModel(SlackDescriptor.KEY_CHANNEL_NAME, "Alert channel");
        ConfigurationFieldModel username = createFieldModel(SlackDescriptor.KEY_CHANNEL_USERNAME, "Alert unit test");
        ConfigurationFieldModel webhook = createFieldModel(SlackDescriptor.KEY_WEBHOOK, "Webhook");

        fields.add(channel);
        fields.add(username);
        fields.add(webhook);

        Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("Slack Test Job", ChannelKeys.SLACK.getUniversalKey());
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createEmailDistributionFieldsProjectOwnerOnly() {
        List<ConfigurationFieldModel> fields = new ArrayList<>();

        ConfigurationFieldModel emailAddresses = createFieldModel(EmailDescriptor.KEY_EMAIL_ADDRESSES, List.of("noreply@blackducksoftware.com"));
        ConfigurationFieldModel projectOwnerOnly = createFieldModel(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, "true");
        ConfigurationFieldModel subjectLine = createFieldModel(EmailDescriptor.KEY_SUBJECT_LINE, "Alert unit test subject line");

        fields.add(emailAddresses);
        fields.add(projectOwnerOnly);
        fields.add(subjectLine);

        Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("Email Test Job", ChannelKeys.EMAIL.getUniversalKey());
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createEmailDistributionFields() {
        List<ConfigurationFieldModel> fields = new ArrayList<>();

        ConfigurationFieldModel emailAddresses = createFieldModel(EmailDescriptor.KEY_EMAIL_ADDRESSES, List.of("noreply@blackducksoftware.com"));
        ConfigurationFieldModel projectOwnerOnly = createFieldModel(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, "false");
        ConfigurationFieldModel subjectLine = createFieldModel(EmailDescriptor.KEY_SUBJECT_LINE, "Alert unit test subject line");

        fields.add(emailAddresses);
        fields.add(projectOwnerOnly);
        fields.add(subjectLine);

        Collection<ConfigurationFieldModel> commonFields = createCommonDistributionFields("Email Test Job", ChannelKeys.EMAIL.getUniversalKey());
        fields.addAll(commonFields);
        return fields;
    }

    public static List<ConfigurationFieldModel> createBlackDuckDistributionFields() {
        List<ConfigurationFieldModel> fields = new ArrayList<>();

        ConfigurationFieldModel notificationTypes = createFieldModel(ProviderDescriptor.KEY_NOTIFICATION_TYPES, List.of(NotificationType.VULNERABILITY.toString(), NotificationType.RULE_VIOLATION.toString()));
        ConfigurationFieldModel formatType = createFieldModel(ProviderDescriptor.KEY_PROCESSING_TYPE, ProcessingType.DEFAULT.toString());
        ConfigurationFieldModel filterByProject = createFieldModel(ProviderDescriptor.KEY_FILTER_BY_PROJECT, "true");
        ConfigurationFieldModel projectNamePattern = createFieldModel(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, ".*UnitTest.*");
        ConfigurationFieldModel configuredProject = createFieldModel(ProviderDescriptor.KEY_CONFIGURED_PROJECT, List.of("TestProject1", "TestProject2"));

        fields.add(notificationTypes);
        fields.add(formatType);
        fields.add(filterByProject);
        fields.add(projectNamePattern);
        fields.add(configuredProject);

        return fields;
    }

    public static List<ConfigurationFieldModel> createCommonDistributionFields(String jobName, String channelDescriptorName) {
        List<ConfigurationFieldModel> fields = new ArrayList<>();

        ConfigurationFieldModel name = createFieldModel(ChannelDescriptor.KEY_NAME, jobName);
        ConfigurationFieldModel channelName = createFieldModel(ChannelDescriptor.KEY_CHANNEL_NAME, channelDescriptorName);
        ConfigurationFieldModel providerName = createFieldModel(ChannelDescriptor.KEY_PROVIDER_TYPE, new BlackDuckProviderKey().getUniversalKey());
        ConfigurationFieldModel frequencyType = createFieldModel(ChannelDescriptor.KEY_FREQUENCY, FrequencyType.REAL_TIME.toString());

        ConfigurationFieldModel providerNotificationType = createFieldModel(ProviderDescriptor.KEY_NOTIFICATION_TYPES, NotificationType.POLICY_OVERRIDE.name());
        ConfigurationFieldModel processingType = createFieldModel(ProviderDescriptor.KEY_PROCESSING_TYPE, ProcessingType.DEFAULT.name());

        fields.add(name);
        fields.add(channelName);
        fields.add(providerName);
        fields.add(frequencyType);
        fields.add(providerNotificationType);
        fields.add(processingType);

        return fields;
    }

    public static ConfigurationModel createCommonConfigModel(Long id, Long descriptorId, String distributionType, String name, String providerName, String frequency,
        String filterByProject, String projectNamePattern, List<String> configuredProjects, List<String> notificationTypes, String formatType) {
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);

        Mockito.when(configurationModel.getConfigurationId()).thenReturn(id);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(descriptorId);

        List<ConfigurationFieldModel> fieldList = new ArrayList<>();
        mockField(fieldList, configurationModel, ChannelDescriptor.KEY_NAME, name);
        mockField(fieldList, configurationModel, ChannelDescriptor.KEY_FREQUENCY, frequency);
        mockField(fieldList, configurationModel, ChannelDescriptor.KEY_PROVIDER_TYPE, providerName);
        mockField(fieldList, configurationModel, ChannelDescriptor.KEY_CHANNEL_NAME, distributionType);

        mockField(fieldList, configurationModel, ProviderDescriptor.KEY_NOTIFICATION_TYPES, notificationTypes);
        mockField(fieldList, configurationModel, ProviderDescriptor.KEY_PROCESSING_TYPE, formatType);

        mockField(fieldList, configurationModel, ProviderDescriptor.KEY_FILTER_BY_PROJECT, filterByProject);
        mockField(fieldList, configurationModel, ProviderDescriptor.KEY_PROJECT_NAME_PATTERN, projectNamePattern);
        mockField(fieldList, configurationModel, ProviderDescriptor.KEY_CONFIGURED_PROJECT, configuredProjects);

        Mockito.when(configurationModel.getConfigurationId()).thenReturn(id);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(descriptorId);
        Mockito.when(configurationModel.getCopyOfFieldList()).thenReturn(fieldList);
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fieldList));

        return configurationModel;
    }

    private static void mockField(List<ConfigurationFieldModel> fieldList, ConfigurationModel configurationModel, String key, String value) {
        mockField(fieldList, configurationModel, key, List.of(value));
    }

    private static void mockField(List<ConfigurationFieldModel> fieldList, ConfigurationModel configurationModel, String key, Collection<String> values) {
        ConfigurationFieldModel field = ConfigurationFieldModel.create(key);
        field.setFieldValues(values);
        Mockito.when(configurationModel.getField(key)).thenReturn(Optional.of(field));
        fieldList.add(field);
    }

    public static Map<String, ConfigurationFieldModel> mapFieldKeyToFields(Collection<ConfigurationFieldModel> fields) {
        return DataStructureUtils.mapToValues(fields, ConfigurationFieldModel::getFieldKey);
    }

    public static Map<String, ConfigurationFieldModel> mapStringsToFields(Map<String, String> fields) {
        Map<String, ConfigurationFieldModel> configurationFieldMap = new HashMap<>(fields.size());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(entry.getKey());
            configurationFieldModel.setFieldValue(entry.getValue());
            configurationFieldMap.put(entry.getKey(), configurationFieldModel);
        }

        return configurationFieldMap;
    }

    public static Map<String, ConfigurationFieldModel> mapStringsToSensitiveFields(Map<String, String> fields) {
        Map<String, ConfigurationFieldModel> configurationFieldMap = new HashMap<>(fields.size());
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.createSensitive(entry.getKey());
            configurationFieldModel.setFieldValue(entry.getValue());
            configurationFieldMap.put(entry.getKey(), configurationFieldModel);
        }

        return configurationFieldMap;
    }

    public static ConfigurationFieldModel createFieldModel(String fieldKey, Collection<String> fieldValues) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
        configurationFieldModel.setFieldValues(fieldValues);
        return configurationFieldModel;
    }

    public static ConfigurationFieldModel createSensitiveFieldModel(String fieldKey, Collection<String> fieldValues) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.createSensitive(fieldKey);
        configurationFieldModel.setFieldValues(fieldValues);
        return configurationFieldModel;
    }

    public static ConfigurationFieldModel createFieldModel(String fieldKey, String fieldValue) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
        configurationFieldModel.setFieldValue(fieldValue);
        return configurationFieldModel;
    }

    public static ConfigurationFieldModel createSensitiveFieldModel(String fieldKey, String fieldValue) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.createSensitive(fieldKey);
        configurationFieldModel.setFieldValue(fieldValue);
        return configurationFieldModel;
    }

}
