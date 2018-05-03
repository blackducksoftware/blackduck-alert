package com.blackducksoftware.integration.hub.alert.digest.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.notification.NotificationCategoryEnum;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category({ DatabaseConnectionTest.class, ExternalConnectionTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationEventManagerTest {

    @Autowired
    private CommonDistributionRepositoryWrapper commonDistributionRepository;

    @Autowired
    private NotificationEventManager notificationEventMananger;

    @Autowired
    private DistributionNotificationTypeRepositoryWrapper distributionNotificationTypeRepository;

    @Autowired
    private NotificationTypeRepositoryWrapper notificationTypeRepository;

    @Before
    public void initializeConfig() {
        long configId = 1;
        CommonDistributionConfigEntity slackDistributionConfig = new CommonDistributionConfigEntity(configId++, SupportedChannels.SLACK, "Slack Config", DigestTypeEnum.REAL_TIME, false);
        CommonDistributionConfigEntity hipChatDistributionConfig = new CommonDistributionConfigEntity(configId++, SupportedChannels.HIPCHAT, "HipChat Config", DigestTypeEnum.REAL_TIME, false);
        CommonDistributionConfigEntity emailDistributionConfig = new CommonDistributionConfigEntity(configId++, SupportedChannels.EMAIL_GROUP, "Email Config", DigestTypeEnum.REAL_TIME, false);

        slackDistributionConfig = commonDistributionRepository.save(slackDistributionConfig);
        hipChatDistributionConfig = commonDistributionRepository.save(hipChatDistributionConfig);
        emailDistributionConfig = commonDistributionRepository.save(emailDistributionConfig);

        for (final NotificationCategoryEnum notificationCategoryEnum : NotificationCategoryEnum.values()) {
            final NotificationTypeEntity notificationTypeEntity = new NotificationTypeEntity(notificationCategoryEnum);
            final NotificationTypeEntity savedNotificationType = notificationTypeRepository.save(notificationTypeEntity);
            saveDistributionNotificationTypeRelation(slackDistributionConfig.getId(), savedNotificationType.getId());
            saveDistributionNotificationTypeRelation(hipChatDistributionConfig.getId(), savedNotificationType.getId());
            saveDistributionNotificationTypeRelation(emailDistributionConfig.getId(), savedNotificationType.getId());
        }
    }

    private void saveDistributionNotificationTypeRelation(final Long commonDistributionConfigId, final Long notificationTypeId) {
        final DistributionNotificationTypeRelation notificationRelation = new DistributionNotificationTypeRelation(commonDistributionConfigId, notificationTypeId);
        distributionNotificationTypeRepository.save(notificationRelation);
    }

    @Test
    public void createInvalidDigestTypeTest() {
        final ProjectData projectData_email = createProjectData("Project_1", "1.0.0", DigestTypeEnum.DAILY);
        final List<ProjectData> projectDataCollection = Arrays.asList(projectData_email);
        final List<AbstractChannelEvent> channelEvents = notificationEventMananger.createChannelEvents(projectDataCollection);
        assertTrue(channelEvents.isEmpty());
    }

    @Test
    public void createChannelEventTest() {
        final List<CommonDistributionConfigEntity> configEntityList = commonDistributionRepository.findAll();
        final ProjectData projectData_1 = createProjectData("Project_1", "1.0.0", DigestTypeEnum.REAL_TIME);
        final ProjectData projectData_2 = createProjectData("Project_2", "1.0.0", DigestTypeEnum.REAL_TIME);
        final ProjectData projectData_3 = createProjectData("Project_1", "2.0.0", DigestTypeEnum.REAL_TIME);
        final List<ProjectData> projectDataCollection = Arrays.asList(projectData_1, projectData_2, projectData_3);
        final List<AbstractChannelEvent> channelEvents = notificationEventMananger.createChannelEvents(projectDataCollection);
        assertEquals(configEntityList.size(), channelEvents.size());

        channelEvents.forEach(event -> {
            assertEquals(projectDataCollection, event.getProjectData());
        });

    }

    private ProjectData createProjectData(final String projectName, final String projectVersion, final DigestTypeEnum digestType) {
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        categoryMap.put(NotificationCategoryEnum.HIGH_VULNERABILITY, createCategoryData());
        final ProjectData projectData = new ProjectData(digestType, projectName, projectVersion, null, categoryMap);

        return projectData;
    }

    private CategoryData createCategoryData() {
        final Map<String, Object> itemDataDataSet = new HashMap<>();
        itemDataDataSet.put(ProjectDataFactory.VULNERABILITY_COUNT_KEY_ADDED, 1);
        itemDataDataSet.put(ProjectDataFactory.VULNERABILITY_COUNT_KEY_UPDATED, 1);
        itemDataDataSet.put(ProjectDataFactory.VULNERABILITY_COUNT_KEY_DELETED, 1);

        final CategoryData categoryData = new CategoryData("key", Sets.newLinkedHashSet(new ItemData(itemDataDataSet)), 0);

        return categoryData;
    }
}
