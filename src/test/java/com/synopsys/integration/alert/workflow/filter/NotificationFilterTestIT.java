package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor;
import com.synopsys.integration.alert.database.api.configuration.DescriptorAccessor.RegisteredDescriptorModel;
import com.synopsys.integration.alert.database.channel.JobConfigReader;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.ConfiguredProjectsRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.repository.DistributionProjectRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationFilterTestIT extends AlertIntegrationTest {
    private static final String TEST_DESCRIPTOR_NAME = "Test Descriptor";
    private static final String TEST_DESCRIPTOR_FIELD_KEY = "testFieldKeyForDesc";
    private static final ConfigContextEnum TEST_DESCRIPTOR_FIELD_CONTEXT = ConfigContextEnum.DISTRIBUTION;
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final Date NEW = new Date(2000L);
    private static final Date OLD = new Date(1000L);

    @Autowired
    private JsonExtractor jsonExtractor;

    @Autowired
    private List<ProviderDescriptor> providerDescriptors;

    @Autowired
    private JobConfigReader jobConfigReader;

    @Autowired
    private DescriptorAccessor descriptorAccessor;

    @Autowired
    private ConfigurationAccessor configurationAccessor;

    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;

    @Autowired
    private DistributionProjectRepository distributionProjectRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private DistributionNotificationTypeRepository distributionNotificationTypeRepository;

    private NotificationFilter defaultNotificationFilter;

    @BeforeAll
    public void init() throws AlertDatabaseConstraintException {
        defaultNotificationFilter = new NotificationFilter(jsonExtractor, providerDescriptors, jobConfigReader);
        descriptorAccessor.registerDescriptor(TEST_DESCRIPTOR_NAME, DescriptorType.CHANNEL, List.of());

        final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.getRegisteredDescriptorByName(TEST_DESCRIPTOR_NAME).orElseThrow();
        final DefinedFieldModel definedFieldModel = new DefinedFieldModel(TEST_DESCRIPTOR_FIELD_KEY, TEST_DESCRIPTOR_FIELD_CONTEXT, Boolean.FALSE);
        descriptorAccessor.addDescriptorField(registeredDescriptorModel.getId(), definedFieldModel);
    }

    @AfterAll
    public void destroy() throws AlertDatabaseConstraintException {
        descriptorAccessor.unregisterDescriptor(TEST_DESCRIPTOR_NAME);
    }

    @AfterEach
    public void cleanup() {
        configuredProjectsRepository.deleteAll();
        distributionProjectRepository.deleteAll();
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
    }

    @Test
    public void shortCircuitIfNoCommonConfigsTest() {
        final JobConfigReader jobConfigReader = Mockito.mock(JobConfigReader.class);
        Mockito.when(jobConfigReader.getPopulatedConfigs()).thenReturn(List.of());

        final NotificationFilter notificationFilter = new NotificationFilter(jsonExtractor, providerDescriptors, jobConfigReader);
        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, List.of(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoCommonConfigsForFrequencyTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel fieldModel = createFieldModel(TEST_DESCRIPTOR_FIELD_KEY, "value");
        configurationAccessor.createConfiguration(TEST_DESCRIPTOR_NAME, TEST_DESCRIPTOR_FIELD_CONTEXT, List.of(fieldModel));

        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<NotificationContent> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, Arrays.asList(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoConfiguredNotificationsTest() {
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();

        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<NotificationContent> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, Arrays.asList(applicableNotification));
        assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void applyWithOutOfOrderNotificationsTest() {
        final NotificationContent applicableNotification1 = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final NotificationContent applicableNotification2 = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, OLD);
        final List<NotificationContent> notifications = Arrays.asList(applicableNotification1, applicableNotification2);

        final Collection<NotificationContent> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, notifications);

        assertEquals(2, filteredNotifications.size());
        final List<NotificationContent> randomAccessNotifications = filteredNotifications.stream().collect(Collectors.toList());
        assertEquals(applicableNotification2, randomAccessNotifications.get(0));
        assertEquals(applicableNotification1, randomAccessNotifications.get(1));
    }

    @Test
    public void applyWithOneValidNotificationTest() {
        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final NotificationContent garbage1 = createVulnerabilityNotification("garbage1", BlackDuckProvider.COMPONENT_NAME, new Date());
        final NotificationContent garbage2 = createVulnerabilityNotification("garbage2", BlackDuckProvider.COMPONENT_NAME, new Date());
        final NotificationContent garbage3 = createVulnerabilityNotification("garbage3", BlackDuckProvider.COMPONENT_NAME, new Date());
        final List<NotificationContent> notifications = Arrays.asList(garbage1, applicableNotification, garbage2, garbage3);

        // TODO refactor the test to use the ObjectHierarchicalField

        final Collection<NotificationContent> filteredNotifications = defaultNotificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, notifications);

        assertEquals(1, filteredNotifications.size());
        assertEquals(applicableNotification, filteredNotifications.iterator().next());
    }

    private ConfigurationFieldModel createFieldModel(final String fieldKey, final String fieldValue) {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(fieldKey);
        field.setFieldValue(fieldValue);
        return field;
    }

    private NotificationContent createVulnerabilityNotification(final String projectName, final String providerName, final Date created) {
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
        final NotificationContent notification = new NotificationContent(created, providerName, created, NotificationType.VULNERABILITY.name(), content);
        notification.setId(1L);

        return notification;
    }
}
