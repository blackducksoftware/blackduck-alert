package com.synopsys.integration.alert.workflow.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.ConfiguredProjectEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.ConfiguredProjectsRepository;
import com.synopsys.integration.alert.database.entity.repository.NotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.DistributionNotificationTypeRelation;
import com.synopsys.integration.alert.database.relation.DistributionProjectRelation;
import com.synopsys.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;
import com.synopsys.integration.alert.database.relation.repository.DistributionProjectRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationFilterTestIT extends AlertIntegrationTest {
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final Date NEW = new Date(2000L);
    private static final Date OLD = new Date(1000L);

    @Autowired
    private NotificationFilter notificationFilter;

    @Autowired
    private HipChatDistributionRepository hipChatDistributionRepository;

    @Autowired
    private CommonDistributionRepository commonDistributionRepository;

    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;

    @Autowired
    private DistributionProjectRepository distributionProjectRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private DistributionNotificationTypeRepository distributionNotificationTypeRepository;

    private Long commonDistributionId = 0L;
    private Long distributionConfigId = 0L;
    private Long projectId = 0L;
    private String notificationType = "";

    @Before
    public void init() {
        final HipChatDistributionConfigEntity hipChatEntity = createHipChatEntity();
        final HipChatDistributionConfigEntity savedHipChatEntity = hipChatDistributionRepository.save(hipChatEntity);
        distributionConfigId = savedHipChatEntity.getId();

        final CommonDistributionConfigEntity configEntity = createCommonConfigEntity();
        configEntity.setDistributionConfigId(distributionConfigId);
        final CommonDistributionConfigEntity savedCommonEntity = commonDistributionRepository.save(configEntity);
        commonDistributionId = savedCommonEntity.getId();

        final ConfiguredProjectEntity projectEntity = new ConfiguredProjectEntity(TEST_PROJECT_NAME);
        final ConfiguredProjectEntity savedProjectEntity = configuredProjectsRepository.save(projectEntity);
        projectId = savedProjectEntity.getId();

        final DistributionProjectRelation distributionProjectRelation = new DistributionProjectRelation(commonDistributionId, projectId);
        distributionProjectRepository.save(distributionProjectRelation);

        notificationType = NotificationType.VULNERABILITY.name();

        final DistributionNotificationTypeRelation typeRelation = new DistributionNotificationTypeRelation(commonDistributionId, notificationType);
        distributionNotificationTypeRepository.save(typeRelation);
    }

    @After
    public void cleanup() {
        hipChatDistributionRepository.deleteAll();
        commonDistributionRepository.deleteAll();
        configuredProjectsRepository.deleteAll();
        distributionProjectRepository.deleteAll();
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
    }

    @Test
    public void shortCircuitIfNoCommonConfigsTest() {
        commonDistributionRepository.deleteAll();

        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, Arrays.asList(applicableNotification));
        Assert.assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoCommonConfigsForFrequencyTest() {
        final Optional<CommonDistributionConfigEntity> foundEntity = commonDistributionRepository.findById(commonDistributionId);
        if (foundEntity.isPresent()) {
            final CommonDistributionConfigEntity commonEntity = foundEntity.get();
            final CommonDistributionConfigEntity newEntity = new CommonDistributionConfigEntity(commonEntity.getDistributionConfigId(), commonEntity.getDistributionType(), commonEntity.getName(), BlackDuckProvider.COMPONENT_NAME,
                FrequencyType.DAILY, commonEntity.getFilterByProject(), commonEntity.getProjectNamePattern(), FormatType.DEFAULT);
            newEntity.setId(commonEntity.getId());
            commonDistributionRepository.save(newEntity);
        }

        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, Arrays.asList(applicableNotification));
        Assert.assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void shortCircuitIfNoConfiguredNotificationsTest() {
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();

        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, Arrays.asList(applicableNotification));
        Assert.assertEquals(0, filteredNotifications.size());
    }

    @Test
    public void applyWithOutOfOrderNotificationsTest() {
        final NotificationContent applicableNotification1 = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final NotificationContent applicableNotification2 = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, OLD);
        final List<NotificationContent> notifications = Arrays.asList(applicableNotification1, applicableNotification2);

        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, notifications);

        Assert.assertEquals(2, filteredNotifications.size());
        final List<NotificationContent> randomAccessNotifications = filteredNotifications.stream().collect(Collectors.toList());
        Assert.assertEquals(applicableNotification2, randomAccessNotifications.get(0));
        Assert.assertEquals(applicableNotification1, randomAccessNotifications.get(1));
    }

    @Test
    public void applyWithOneValidNotificationTest() {
        final NotificationContent applicableNotification = createVulnerabilityNotification(TEST_PROJECT_NAME, BlackDuckProvider.COMPONENT_NAME, NEW);
        final NotificationContent garbage1 = createVulnerabilityNotification("garbage1", BlackDuckProvider.COMPONENT_NAME, new Date());
        final NotificationContent garbage2 = createVulnerabilityNotification("garbage2", BlackDuckProvider.COMPONENT_NAME, new Date());
        final NotificationContent garbage3 = createVulnerabilityNotification("garbage3", BlackDuckProvider.COMPONENT_NAME, new Date());
        final List<NotificationContent> notifications = Arrays.asList(garbage1, applicableNotification, garbage2, garbage3);

        //TODO refactor the test to use the ObjectHierarchicalField

        final Collection<NotificationContent> filteredNotifications = notificationFilter.extractApplicableNotifications(FrequencyType.REAL_TIME, notifications);

        Assert.assertEquals(1, filteredNotifications.size());
        Assert.assertEquals(applicableNotification, filteredNotifications.iterator().next());
    }

    private HipChatDistributionConfigEntity createHipChatEntity() {
        final Integer roomId = 12345;
        final String color = "green";
        final HipChatDistributionConfigEntity entity = new HipChatDistributionConfigEntity(roomId, Boolean.FALSE, color);

        return entity;
    }

    private CommonDistributionConfigEntity createCommonConfigEntity() {
        final String distributionType = HipChatChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String name = "name";
        final FrequencyType frequency = FrequencyType.REAL_TIME;

        final CommonDistributionConfigEntity entity = new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, providerName, frequency, Boolean.TRUE, "", FormatType.DEFAULT);

        return entity;
    }

    private NotificationContent createVulnerabilityNotification(final String projectName, final String providerName, final Date created) {
        final String content = "{\"content\":{\"affectedProjectVersions\":[{\"projectName\":\""
                                   + projectName
                                   + "\",\"dummyField\":\"dummyValue\"},{\"projectName\":\"Project Name\",\"dummyField\":\"dummyValue\"}],\"dummyField\":\"dummyValue\"},\"dummyField\":\"dummyValue\"}";
        final NotificationContent notification = new NotificationContent(created, providerName, created, NotificationType.VULNERABILITY.name(), content);
        notification.setId(1L);

        return notification;
    }
}
