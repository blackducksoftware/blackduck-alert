package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.database.notification.NotificationContent;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.DescriptorMocker;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationFilterTestIT extends AlertIntegrationTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();
    private static final String TEST_DESCRIPTOR_NAME = new SlackChannelKey().getUniversalKey();
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

    private NotificationFilter defaultNotificationFilter;
    private ConfigurationJobModel defaultJob;

    @BeforeEach
    public void init() {
        final List<ConfigurationFieldModel> fieldList = MockConfigurationModelFactory.createBlackDuckDistributionFields();
        fieldList.addAll(MockConfigurationModelFactory.createSlackDistributionFields());
        final ConfigurationJobModel distributionJob = MockConfigurationModelFactory.createDistributionJob(fieldList);
        final ConfigurationFieldModel frequency = distributionJob.getFieldAccessor().getField(ChannelDistributionUIConfig.KEY_FREQUENCY)
                                                      .orElse(ConfigurationFieldModel.create(ChannelDistributionUIConfig.KEY_FREQUENCY));
        frequency.setFieldValues(List.of(TEST_CONFIG_FREQUENCY.name()));
        final ConfigurationFieldModel notificationType = distributionJob.getFieldAccessor().getField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES)
                                                             .orElse(ConfigurationFieldModel.create(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES));
        notificationType.setFieldValues(List.of(TEST_CONFIG_NOTIFICATION_TYPE));
        final ConfigurationFieldModel project = distributionJob.getFieldAccessor().getField(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT).orElse(ConfigurationFieldModel.create(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT));
        project.setFieldValues(List.of(TEST_CONFIG_PROJECT_NAME));
        distributionJob.getFieldAccessor().addFields(Map.of(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, notificationType, ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, project));

        defaultJob = distributionJob;

        defaultNotificationFilter = new NotificationFilter(jsonExtractor);

        descriptorMocker.registerDescriptor(TEST_DESCRIPTOR_NAME, DescriptorType.CHANNEL);
        descriptorMocker.addFieldToDescriptor(TEST_DESCRIPTOR_NAME, TEST_DESCRIPTOR_FIELD_KEY, TEST_DESCRIPTOR_FIELD_CONTEXT, Boolean.FALSE);
    }

    @AfterEach
    public void cleanup() {
        descriptorMocker.cleanUpDescriptors();
    }

    @Test
    public void shortCircuitIfNoNotificationTypesTest() {
        final List<ConfigurationFieldModel> fieldList = MockConfigurationModelFactory.createBlackDuckDistributionFields();
        fieldList.addAll(MockConfigurationModelFactory.createSlackDistributionFields());
        final ConfigurationJobModel distributionJob = MockConfigurationModelFactory.createDistributionJob(fieldList);

        final NotificationFilter notificationFilter = new NotificationFilter(jsonExtractor);
        final AlertNotificationWrapper applicableNotification = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), NEW);
        final Collection<AlertNotificationWrapper> filteredNotifications = notificationFilter.extractApplicableNotifications(getProviderContentTypes(), distributionJob, List.of(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoMatchingNotificationTypeTest() {
        final AlertNotificationWrapper applicableNotification = new NotificationContent(NEW, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), NEW, NotificationType.BOM_EDIT.name(), "{}");
        final Collection<AlertNotificationWrapper> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(getProviderContentTypes(), defaultJob, List.of(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void applyWithOutOfOrderNotificationsTest() {
        final AlertNotificationWrapper applicableNotification1 = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), NEW);
        final AlertNotificationWrapper applicableNotification2 = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), OLD);
        final List<AlertNotificationWrapper> notifications = List.of(applicableNotification1, applicableNotification2);

        final Collection<AlertNotificationWrapper> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(getProviderContentTypes(), defaultJob, notifications);

        assertEquals(2, filteredNotifications.size());
        final List<AlertNotificationWrapper> randomAccessNotifications = new ArrayList<>(filteredNotifications);
        assertEquals(applicableNotification2, randomAccessNotifications.get(0));
        assertEquals(applicableNotification1, randomAccessNotifications.get(1));
    }

    @Test
    public void applyWithOneValidNotificationTest() {
        final AlertNotificationWrapper applicableNotification = createVulnerabilityNotification(TEST_CONFIG_PROJECT_NAME, BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), NEW);
        final AlertNotificationWrapper garbage1 = createVulnerabilityNotification("garbage1", BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new Date());
        final AlertNotificationWrapper garbage2 = createVulnerabilityNotification("garbage2", BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new Date());
        final AlertNotificationWrapper garbage3 = createVulnerabilityNotification("garbage3", BLACK_DUCK_PROVIDER_KEY.getUniversalKey(), new Date());
        final List<AlertNotificationWrapper> notifications = List.of(garbage1, applicableNotification, garbage2, garbage3);

        // TODO refactor the test to use the ObjectHierarchicalField

        final Collection<AlertNotificationWrapper> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(getProviderContentTypes(), defaultJob, notifications);

        assertEquals(1, filteredNotifications.size());
        assertEquals(applicableNotification, filteredNotifications.iterator().next());
    }

    private Set<ProviderContentType> getProviderContentTypes() {
        return providers
                   .parallelStream()
                   .map(Provider::getProviderContent)
                   .map(ProviderContent::getContentTypes)
                   .flatMap(Set::stream)
                   .collect(Collectors.toSet());
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
