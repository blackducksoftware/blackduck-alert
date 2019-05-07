package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.database.api.DefaultConfigurationAccessor;
import com.synopsys.integration.alert.database.api.JobConfigReader;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.DescriptorMocker;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationFilterTestIT extends AlertIntegrationTest {
    private static final String TEST_DESCRIPTOR_NAME = HipChatChannel.COMPONENT_NAME;
    private static final String TEST_DESCRIPTOR_FIELD_KEY = "testFieldKeyForDesc";
    private static final ConfigContextEnum TEST_DESCRIPTOR_FIELD_CONTEXT = ConfigContextEnum.DISTRIBUTION;

    private static final FrequencyType TEST_CONFIG_FREQUENCY = FrequencyType.REAL_TIME;
    private static final String TEST_CONFIG_PROJECT_NAME = "Test Project";
    private static final String TEST_CONFIG_NOTIFICATION_TYPE = NotificationType.VULNERABILITY.name();

    private static final Date NEW = new Date(2000L);
    private static final Date OLD = new Date(1000L);

    @Autowired
    private JsonExtractor jsonExtractor;

    @Autowired
    private List<Provider> providers;

    @Autowired
    private DescriptorMocker descriptorMocker;

    @Autowired
    private DefaultConfigurationAccessor configurationAccessor;

    private NotificationFilter defaultNotificationFilter;

    @BeforeEach
    public void init() throws AlertDatabaseConstraintException {
        final List<ConfigurationFieldModel> fieldList = MockConfigurationModelFactory.createBlackDuckDistributionFields();
        fieldList.addAll(MockConfigurationModelFactory.createHipChatDistributionFields());
        final Map<String, ConfigurationFieldModel> fieldMap = MockConfigurationModelFactory.mapFieldKeyToFields(fieldList);
        fieldMap.get(ChannelDistributionUIConfig.KEY_FREQUENCY).setFieldValue(TEST_CONFIG_FREQUENCY.name());
        fieldMap.get(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES).setFieldValue(TEST_CONFIG_NOTIFICATION_TYPE);
        fieldMap.get(CommonDistributionConfiguration.KEY_FILTER_BY_PROJECT).setFieldValue(Boolean.TRUE.toString());
        fieldMap.get(CommonDistributionConfiguration.KEY_CONFIGURED_PROJECT).setFieldValue(TEST_CONFIG_PROJECT_NAME);

        final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(fieldMap);
        Mockito.when(configurationModel.getConfigurationId()).thenReturn(1L);
        final ConfigurationJobModel configurationJobModel = Mockito.mock(ConfigurationJobModel.class);
        Mockito.when(configurationJobModel.getJobId()).thenReturn(UUID.randomUUID());
        Mockito.when(configurationJobModel.getCopyOfConfigurations()).thenReturn(Set.of(configurationModel));
        Mockito.when(configurationJobModel.createKeyToFieldMap()).thenReturn(fieldMap);

        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(configurationJobModel);

        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);
        Mockito.when(jobConfigReader.getPopulatedJobConfigs()).thenReturn(List.of(config));

        defaultNotificationFilter = new NotificationFilter(jsonExtractor, providers, jobConfigReader);

        descriptorMocker.registerDescriptor(TEST_DESCRIPTOR_NAME, DescriptorType.CHANNEL);
        descriptorMocker.addFieldToDescriptor(TEST_DESCRIPTOR_NAME, TEST_DESCRIPTOR_FIELD_KEY, TEST_DESCRIPTOR_FIELD_CONTEXT, Boolean.FALSE);
    }

    @AfterEach
    public void cleanup() {
        descriptorMocker.cleanUpDescriptors();
    }

    @Test
    public void shortCircuitIfNoCommonConfigsTest() {
        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);
        Mockito.when(jobConfigReader.getPopulatedJobConfigs()).thenReturn(List.of());

        final NotificationFilter notificationFilter = new NotificationFilter(jsonExtractor, providers, jobConfigReader);
        final AlertNotificationWrapper applicableNotification = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<AlertNotificationWrapper> filteredNotifications = notificationFilter.extractApplicableNotifications(TEST_CONFIG_FREQUENCY, List.of(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoCommonConfigsForFrequencyTest() throws AlertDatabaseConstraintException {
        final CommonDistributionConfiguration config = Mockito.mock(CommonDistributionConfiguration.class);
        Mockito.when(config.getFrequencyType()).thenReturn(FrequencyType.DAILY);

        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);
        Mockito.when(jobConfigReader.getPopulatedJobConfigs()).thenReturn(List.of(config));

        final NotificationFilter notificationFilter = new NotificationFilter(jsonExtractor, providers, jobConfigReader);
        final ConfigurationFieldModel fieldModel = createFieldModel(TEST_DESCRIPTOR_FIELD_KEY, "value");
        configurationAccessor.createConfiguration(TEST_DESCRIPTOR_NAME, TEST_DESCRIPTOR_FIELD_CONTEXT, List.of(fieldModel));

        final AlertNotificationWrapper applicableNotification = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<AlertNotificationWrapper> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, Arrays.asList(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoConfiguredNotificationsTest() {
        final AlertNotificationWrapper applicableNotification = new NotificationContent(NEW, BlackDuckProvider.COMPONENT_NAME, NEW, NotificationType.BOM_EDIT.name(), "{}");
        final Collection<AlertNotificationWrapper> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(TEST_CONFIG_FREQUENCY, Arrays.asList(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void applyWithOutOfOrderNotificationsTest() {
        final AlertNotificationWrapper applicableNotification1 = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final AlertNotificationWrapper applicableNotification2 = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, OLD);
        final List<AlertNotificationWrapper> notifications = Arrays.asList(applicableNotification1, applicableNotification2);

        final Collection<AlertNotificationWrapper> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(TEST_CONFIG_FREQUENCY, notifications);

        assertEquals(2, filteredNotifications.size());
        final List<AlertNotificationWrapper> randomAccessNotifications = filteredNotifications.stream().collect(Collectors.toList());
        assertEquals(applicableNotification2, randomAccessNotifications.get(0));
        assertEquals(applicableNotification1, randomAccessNotifications.get(1));
    }

    @Test
    public void applyWithOneValidNotificationTest() {
        final AlertNotificationWrapper applicableNotification = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final AlertNotificationWrapper garbage1 = createVulnerabilityNotification("garbage1", BlackDuckProvider.COMPONENT_NAME, new Date());
        final AlertNotificationWrapper garbage2 = createVulnerabilityNotification("garbage2", BlackDuckProvider.COMPONENT_NAME, new Date());
        final AlertNotificationWrapper garbage3 = createVulnerabilityNotification("garbage3", BlackDuckProvider.COMPONENT_NAME, new Date());
        final List<AlertNotificationWrapper> notifications = List.of(garbage1, applicableNotification, garbage2, garbage3);

        // TODO refactor the test to use the ObjectHierarchicalField

        final Collection<AlertNotificationWrapper> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(TEST_CONFIG_FREQUENCY, notifications);

        assertEquals(1, filteredNotifications.size());
        assertEquals(applicableNotification, filteredNotifications.iterator().next());
    }

    private ConfigurationFieldModel createFieldModel(final String fieldKey, final String fieldValue) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(fieldKey);
        field.setFieldValue(fieldValue);
        return field;
    }

    private AlertNotificationWrapper createVulnerabilityNotification(final String projectName, final String providerName, final Date created) {
        final String content = "{\"content\":"
                                   + "{"
                                   + "    \"affectedProjectVersions\":"
                                   + "        ["
                                   + "            {"
                                   + "                \"projectName\": \"" + projectName + "\","
                                   + "                 \"dummyField\":\"dummyValue\""
                                   + "            },"
                                   + "            {"
                                   + "                \"projectName\":\"Project Name\","
                                   + "                \"dummyField\":\"dummyValue\""
                                   + "            }"
                                   + "        ],"
                                   + "    \"dummyField\":\"dummyValue\""
                                   + "},"
                                   + "\"dummyField\":\"dummyValue\""
                                   + "}";
        final NotificationContent notification = new NotificationContent(created, providerName, created, TEST_CONFIG_NOTIFICATION_TYPE, content);
        notification.setId(1L);

        return notification;
    }
}
