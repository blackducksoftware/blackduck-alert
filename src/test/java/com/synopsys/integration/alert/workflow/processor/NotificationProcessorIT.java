/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.workflow.processor;

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
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.ConfiguredProjectsRepository;
import com.synopsys.integration.alert.database.relation.repository.DistributionProjectRepository;
import com.synopsys.integration.alert.web.actions.NotificationTypesActions;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationProcessorIT {
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private DistributionProjectRepository distributionProjectRepository;
    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;
    @Autowired
    private NotificationTypesActions notificationActions;
    @Autowired
    private NotificationProcessor processor;

    @Before
    public void cleanup() {
        commonDistributionRepository.deleteAll();
        distributionProjectRepository.deleteAll();
        configuredProjectsRepository.deleteAll();
        notificationActions.getDistributionNotificationTypeRepository().deleteAll();
        notificationActions.getNotificationTypeRepository().deleteAll();
    }

    @Test
    public void processNotificationDataBasicTestIT() {
        //TODO reimplement when tests
        //        final Long distributionConfigId = 10L;
        //        final String distributionType = HipChatChannel.COMPONENT_NAME;
        //        final String name = "Config Name";
        //        final FrequencyType frequency = FrequencyType.REAL_TIME;
        //        final Boolean filterByProject = true;
        //
        //        final String projectName = "Test Hub Project Name";
        //
        //        final CommonDistributionConfigEntity commonDistributionConfigEntity = commonDistributionRepository.save(new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject));
        //        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        //        distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigEntity.getId(), configuredProjectEntity.getId()));
        //
        //        notificationActions.saveNotificationTypes(commonDistributionConfigEntity.getId(), Arrays.asList("POLICY_VIOLATION"));
        //
        //        final List<NotificationModel> notificationList = new ArrayList<>();
        //        final NotificationEntity applicableNotification = new NotificationEntity("event_key_1", new Date(System.currentTimeMillis()), NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", "", "", "Test Component",
        //                "Test Component Version", "Test Policy Rule Name", "Test Person");
        //        final NotificationEntity nonApplicableNotification = new NotificationEntity("event_key_2", new Date(System.currentTimeMillis()), NotificationCategoryEnum.POLICY_VIOLATION, "Project that we don't care about", "", "", "",
        //                "Test Component", "Test Component Version", "Test Policy Rule Name", "Test Person");
        //        notificationList.add(new NotificationModel(applicableNotification, Collections.emptyList()));
        //        notificationList.add(new NotificationModel(nonApplicableNotification, Collections.emptyList()));
        //
        //        final List<ChannelEvent> eventsCreated = processor.processNotifications(FrequencyType.REAL_TIME, notificationList);
        //        assertEquals(1, eventsCreated.size());
        //        final ChannelEvent event = eventsCreated.get(0);
        //        assertTrue(HipChatChannel.COMPONENT_NAME.equals(event.getDestination()));
        //        assertEquals(commonDistributionConfigEntity.getId(), event.getCommonDistributionConfigId());
    }

    @Test
    public void processNotificationDataWithSameEventKeyTestIT() {
        //TODO reimplement when tests
        //        final Long distributionConfigId = 10L;
        //        final String distributionType = HipChatChannel.COMPONENT_NAME;
        //        final String name = "Config Name";
        //        final FrequencyType frequency = FrequencyType.REAL_TIME;
        //        final Boolean filterByProject = true;
        //
        //        final String eventKey = "event_key";
        //        final String projectName = "Test Hub Project Name";
        //        final String projectVersionName = "Test Hub Project Version Name";
        //
        //        final CommonDistributionConfigEntity commonDistributionConfigEntity = commonDistributionRepository.save(new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject));
        //        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        //        distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigEntity.getId(), configuredProjectEntity.getId()));
        //
        //        notificationActions.saveNotificationTypes(commonDistributionConfigEntity.getId(), Arrays.asList("POLICY_VIOLATION"));
        //
        //        final List<NotificationModel> notificationList = new ArrayList<>();
        //        final NotificationEntity applicableNotification = new NotificationEntity(eventKey, new Date(System.currentTimeMillis()), NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersionName, "", "Test Component",
        //                "Test Component Version", "Test Policy Rule Name", "Test Person");
        //        final NotificationEntity otherApplicableNotification = new NotificationEntity(eventKey, new Date(System.currentTimeMillis()), NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersionName, "", "Test Component",
        //                "Test Component Version", "Test Policy Rule Name", "Test Person");
        //        notificationList.add(new NotificationModel(applicableNotification, Collections.emptyList()));
        //        notificationList.add(new NotificationModel(otherApplicableNotification, Collections.emptyList()));
        //
        //        final List<ChannelEvent> eventsCreated = processor.processNotifications(FrequencyType.REAL_TIME, notificationList);
        //        assertEquals(1, eventsCreated.size());
        //        final ChannelEvent event = eventsCreated.get(0);
        //        assertTrue(HipChatChannel.COMPONENT_NAME.equals(event.getDestination()));
        //        assertEquals(commonDistributionConfigEntity.getId(), event.getCommonDistributionConfigId());
    }

    @Test
    public void processNotificationDataWithNegatingTypesTestIT() {
        //TODO reimplement when tests
        //        final Long distributionConfigId = 10L;
        //        final String distributionType = HipChatChannel.COMPONENT_NAME;
        //        final String name = "Config Name";
        //        final FrequencyType frequency = FrequencyType.REAL_TIME;
        //        final Boolean filterByProject = true;
        //
        //        final String eventKey = "event_key";
        //        final String projectName = "Test Hub Project Name";
        //        final String projectVersionName = "Test Hub Project Version Name";
        //
        //        final CommonDistributionConfigEntity commonDistributionConfigEntity = commonDistributionRepository.save(new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject));
        //        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        //        distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigEntity.getId(), configuredProjectEntity.getId()));
        //
        //        notificationActions.saveNotificationTypes(commonDistributionConfigEntity.getId(), Arrays.asList("POLICY_VIOLATION", "POLICY_VIOLATION_CLEARED"));
        //
        //        final List<NotificationModel> notificationList = new LinkedList<>();
        //        final NotificationEntity applicableNotification = new NotificationEntity(eventKey, new Date(System.currentTimeMillis()), NotificationCategoryEnum.POLICY_VIOLATION, projectName, "", projectVersionName, "", "Test Component",
        //                "Test Component Version", "Test Policy Rule Name", "Test Person");
        //        final NotificationEntity nonApplicableNotification = new NotificationEntity(eventKey, new Date(System.currentTimeMillis()), NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, projectName, "", projectVersionName, "",
        //                "Test Component", "Test Component Version", "Test Policy Rule Name", "Test Person");
        //        notificationList.add(new NotificationModel(applicableNotification, Collections.emptyList()));
        //        notificationList.add(new NotificationModel(nonApplicableNotification, Collections.emptyList()));
        //
        //        final List<ChannelEvent> eventsCreated = processor.processNotifications(FrequencyType.REAL_TIME, notificationList);
        //        assertEquals(0, eventsCreated.size());
    }

}
