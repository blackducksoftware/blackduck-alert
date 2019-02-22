package com.synopsys.integration.alert.workflow.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.JobConfigReader;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.workflow.filter.NotificationFilter;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class MessageContentAggregatorTest extends AlertIntegrationTest {
    @Autowired
    private List<ProviderDescriptor> providerDescriptors;
    @Autowired
    private JobConfigReader jobConfigReader;
    @Autowired
    private NotificationFilter notificationFilter;

    @Test
    public void testNoJobProcessing() throws Exception {
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final AlertNotificationWrapper policyNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, policyContent, NotificationType.RULE_VIOLATION_CLEARED);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final AlertNotificationWrapper vulnerabilityNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<AlertNotificationWrapper> notificationContentList = List.of(policyNotification, vulnerabilityNotification);
        final MessageContentAggregator messageContentAggregator = new MessageContentAggregator(jobConfigReader, providerDescriptors, notificationFilter);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> topicContentMap = messageContentAggregator.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.isEmpty());
    }

    @Test
    public void testJobProcessing() throws Exception {
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final AlertNotificationWrapper policyNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, policyContent, NotificationType.RULE_VIOLATION_CLEARED);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final AlertNotificationWrapper vulnerabilityNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<AlertNotificationWrapper> notificationContentList = List.of(policyNotification, vulnerabilityNotification);

        final List<String> projects = List.of("example", "alert-test-project", "alert-test-project-2");
        final List<String> notificationTypes = List.of(NotificationType.RULE_VIOLATION_CLEARED.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfiguration jobConfig = createCommonDistributionConfiguration(projects, notificationTypes);
        final JobConfigReader spiedReader = Mockito.spy(jobConfigReader);
        Mockito.doReturn(List.of(jobConfig)).when(spiedReader).getPopulatedJobConfigs();

        final MessageContentAggregator messageContentAggregator = new MessageContentAggregator(spiedReader, providerDescriptors, notificationFilter);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> topicContentMap = messageContentAggregator.processNotifications(frequencyType, notificationContentList);

        assertFalse(topicContentMap.isEmpty());
        assertEquals(1, topicContentMap.size());
        assertTrue(topicContentMap.containsKey(jobConfig));
        assertEquals(4, topicContentMap.get(jobConfig).size());
    }

    @Test
    public void testJobProcessingFrequencyMismatch() throws Exception {
        final FrequencyType frequencyType = FrequencyType.DAILY;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final AlertNotificationWrapper policyNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, policyContent, NotificationType.RULE_VIOLATION_CLEARED);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final AlertNotificationWrapper vulnerabilityNotification = createNotification(BlackDuckProvider.COMPONENT_NAME, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<AlertNotificationWrapper> notificationContentList = List.of(policyNotification, vulnerabilityNotification);

        final JobConfigReader spiedReader = Mockito.spy(jobConfigReader);
        Mockito.doReturn(List.of()).when(spiedReader).getPopulatedJobConfigs();

        final MessageContentAggregator messageContentAggregator = new MessageContentAggregator(spiedReader, providerDescriptors, notificationFilter);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> topicContentMap = messageContentAggregator.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.isEmpty());
    }

    @Test
    public void testJobProcessingProviderMismatch() throws Exception {
        final String unknownProvider = "unknown_provider";
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final AlertNotificationWrapper policyNotification = createNotification(unknownProvider, policyContent, NotificationType.RULE_VIOLATION_CLEARED);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final AlertNotificationWrapper vulnerabilityNotification = createNotification(unknownProvider, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<AlertNotificationWrapper> notificationContentList = List.of(policyNotification, vulnerabilityNotification);

        final List<String> projects = List.of("bad-project");
        final List<String> notificationTypes = List.of(NotificationType.RULE_VIOLATION_CLEARED.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfiguration jobConfig = createCommonDistributionConfiguration(projects, notificationTypes);

        final JobConfigReader spiedReader = Mockito.spy(jobConfigReader);
        Mockito.doReturn(List.of(jobConfig)).when(spiedReader).getPopulatedJobConfigs();

        final MessageContentAggregator messageContentAggregator = new MessageContentAggregator(spiedReader, providerDescriptors, notificationFilter);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> topicContentMap = messageContentAggregator.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.containsKey(jobConfig));
        assertTrue(topicContentMap.get(jobConfig).isEmpty());
    }

    @Test
    public void testJobProcessingNotificationTypeMismatch() throws Exception {
        final String unknownProvider = "unknown_provider";
        final FrequencyType frequencyType = FrequencyType.REAL_TIME;
        final String policyContent = getNotificationContentFromFile("json/policyRuleClearedNotification.json");
        final AlertNotificationWrapper policyNotification = createNotification(unknownProvider, policyContent, NotificationType.RULE_VIOLATION_CLEARED);

        final String vulnerabilityContent = getNotificationContentFromFile("json/vulnerabilityTest.json");
        final AlertNotificationWrapper vulnerabilityNotification = createNotification(unknownProvider, vulnerabilityContent, NotificationType.VULNERABILITY);

        final List<AlertNotificationWrapper> notificationContentList = List.of(policyNotification, vulnerabilityNotification);

        final List<String> projects = List.of("bad-project");
        final List<String> notificationTypes = List.of(NotificationType.RULE_VIOLATION_CLEARED.name(), NotificationType.VULNERABILITY.name());
        final CommonDistributionConfiguration jobConfig = createCommonDistributionConfiguration(projects, notificationTypes);

        final JobConfigReader spiedReader = Mockito.spy(jobConfigReader);
        Mockito.doReturn(List.of(jobConfig)).when(spiedReader).getPopulatedJobConfigs();

        final MessageContentAggregator messageContentAggregator = new MessageContentAggregator(spiedReader, providerDescriptors, notificationFilter);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> topicContentMap = messageContentAggregator.processNotifications(frequencyType, notificationContentList);

        assertTrue(topicContentMap.containsKey(jobConfig));
        assertTrue(topicContentMap.get(jobConfig).isEmpty());
    }

    private CommonDistributionConfiguration createCommonDistributionConfiguration(final List<String> projectNames, final List<String> notificationTypes) {
        final ConfigurationJobModel configurationModel = Mockito.mock(ConfigurationJobModel.class);

        Mockito.when(configurationModel.getJobId()).thenReturn(UUID.randomUUID());
        //        Mockito.when(configurationModel.getDescriptorId()).thenReturn(1L);
        // Use this to mock fields if necessarily:
        // final String fieldName;
        // final String expectedValue;
        final Map<String, ConfigurationFieldModel> fieldModelMap = new HashMap<>();
        final ConfigurationFieldModel nameModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel channelModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel providerModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel notificationTypeModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel frequencyTypeModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel formatTypeModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel filterByProjectModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel projectNamePatternModel = Mockito.mock(ConfigurationFieldModel.class);
        final ConfigurationFieldModel configuredProjectsModel = Mockito.mock(ConfigurationFieldModel.class);

        fieldModelMap.put(ChannelDistributionUIConfig.KEY_NAME, nameModel);
        fieldModelMap.put(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, channelModel);
        fieldModelMap.put(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, providerModel);
        fieldModelMap.put(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationTypeModel);
        fieldModelMap.put(ChannelDistributionUIConfig.KEY_FREQUENCY, frequencyTypeModel);
        fieldModelMap.put(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, formatTypeModel);
        fieldModelMap.put(CommonDistributionConfiguration.KEY_FILTER_BY_PROJECT, filterByProjectModel);
        fieldModelMap.put(CommonDistributionConfiguration.KEY_PROJECT_NAME_PATTERN, projectNamePatternModel);
        fieldModelMap.put(CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT, configuredProjectsModel);

        Mockito.when(nameModel.getFieldValue()).thenReturn(Optional.of("Unit Test Job"));
        Mockito.when(channelModel.getFieldValue()).thenReturn(Optional.of(HipChatChannel.COMPONENT_NAME));
        Mockito.when(providerModel.getFieldValue()).thenReturn(Optional.of(BlackDuckProvider.COMPONENT_NAME));
        Mockito.when(notificationTypeModel.getFieldValues()).thenReturn(notificationTypes);
        Mockito.when(frequencyTypeModel.getFieldValue()).thenReturn(Optional.of(FrequencyType.REAL_TIME.name()));
        Mockito.when(formatTypeModel.getFieldValue()).thenReturn(Optional.of(FormatType.DEFAULT.name()));
        Mockito.when(filterByProjectModel.getFieldValue()).thenReturn(Optional.of(String.valueOf(true)));
        Mockito.when(configuredProjectsModel.getFieldValues()).thenReturn(projectNames);

        Mockito.when(configurationModel.createKeyToFieldMap()).thenReturn(fieldModelMap);

        return new CommonDistributionConfiguration(configurationModel);
    }

    private String getNotificationContentFromFile(final String notificationJsonFileName) throws Exception {
        final ClassPathResource classPathResource = new ClassPathResource(notificationJsonFileName);
        final File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

    private AlertNotificationWrapper createNotification(final String providerName, final String notificationContent, final NotificationType type) {
        return new NotificationContent(new Date(), providerName, new Date(), type.name(), notificationContent);
    }
}
