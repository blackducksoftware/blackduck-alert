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
package com.blackducksoftware.integration.hub.alert.digest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserEmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserHipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserSlackRepository;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class DigestNotificationProcessorIT {
    @Autowired
    private HubUsersRepository hubUsersRepository;
    @Autowired
    private HubUserProjectVersionsRepository projectVersionRelationRepository;
    @Autowired
    private HubUserEmailRepository emailRelationRepository;
    @Autowired
    private HubUserHipChatRepository hipChatRelationRepository;
    @Autowired
    private HubUserSlackRepository slackRelationRepository;
    @Autowired
    private HipChatRepository hipChatRepository;
    @Autowired
    private DigestNotificationProcessor processor;

    @After
    public void cleanup() {
        hubUsersRepository.deleteAll();
        projectVersionRelationRepository.deleteAll();
        emailRelationRepository.deleteAll();
        hipChatRelationRepository.deleteAll();
        slackRelationRepository.deleteAll();
        hipChatRepository.deleteAll();
    }

    @Test
    public void processNotificationDataBasicTestIT() {
        final String userName = "sysadmin";
        final String projectName = "Test Hub Project Name";
        final String projectVersionName = "Test Hub Project Version Name";

        final String hipChatApiKey = "test_api_key";
        final Integer hipChatRoomId = 12345;

        final HubUsersEntity userEntity = hubUsersRepository.save(new HubUsersEntity(userName, null));
        final HipChatConfigEntity hipChatConfEnt = hipChatRepository.save(new HipChatConfigEntity(hipChatApiKey, hipChatRoomId, false, "random"));
        hipChatRelationRepository.save(new HubUserHipChatRelation(userEntity.getId(), hipChatConfEnt.getId()));
        projectVersionRelationRepository.save(new HubUserProjectVersionsRelation(userEntity.getId(), projectName, projectVersionName, Boolean.TRUE));

        final List<NotificationEntity> notificationList = new ArrayList<>();
        // TODO load this from json
        final NotificationEntity applicableNotification = new NotificationEntity(userName, "event_key_1", new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        final NotificationEntity nonApplicableNotification = new NotificationEntity(userName, "event_key_2", new Date(), "POLICY_VIOLATION", "Project that we don't care about", "", projectVersionName, "", "Test Component",
                "Test Component Version", "Test Policy Rule Name", "Test Person", Collections.emptyList());
        notificationList.add(applicableNotification);
        notificationList.add(nonApplicableNotification);

        final List<AbstractChannelEvent> eventsCreated = processor.processNotifications(DigestTypeEnum.REAL_TIME, notificationList);
        assertEquals(1, eventsCreated.size());
        final AbstractChannelEvent event = eventsCreated.get(0);
        assertTrue(event instanceof HipChatEvent);
    }

    @Test
    public void processNotificationDataWithSameEventKeyTestIT() {
        final String eventKey = "event_key";

        final String userName = "sysadmin";
        final String projectName = "Test Hub Project Name";
        final String projectVersionName = "Test Hub Project Version Name";

        final String hipChatApiKey = "test_api_key";
        final Integer hipChatRoomId = 12345;

        final HubUsersEntity userEntity = hubUsersRepository.save(new HubUsersEntity(userName, null));
        final HipChatConfigEntity hipChatConfEnt = hipChatRepository.save(new HipChatConfigEntity(hipChatApiKey, hipChatRoomId, false, "random"));
        hipChatRelationRepository.save(new HubUserHipChatRelation(userEntity.getId(), hipChatConfEnt.getId()));
        projectVersionRelationRepository.save(new HubUserProjectVersionsRelation(userEntity.getId(), projectName, projectVersionName, Boolean.TRUE));

        final List<NotificationEntity> notificationList = new ArrayList<>();
        // TODO load this from json
        final NotificationEntity applicableNotification = new NotificationEntity(userName, eventKey, new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        final NotificationEntity otherApplicableNotification = new NotificationEntity(userName, eventKey, new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        notificationList.add(applicableNotification);
        notificationList.add(otherApplicableNotification);

        final List<AbstractChannelEvent> eventsCreated = processor.processNotifications(DigestTypeEnum.REAL_TIME, notificationList);
        assertEquals(1, eventsCreated.size());
        final AbstractChannelEvent event = eventsCreated.get(0);
        assertTrue(event instanceof HipChatEvent);
    }

    @Test
    public void processNotificationDataWithNegatingTypesTestIT() {
        final String eventKey = "event_key";

        final String userName = "sysadmin";
        final String projectName = "Test Hub Project Name";
        final String projectVersionName = "Test Hub Project Version Name";

        final String hipChatApiKey = "test_api_key";
        final Integer hipChatRoomId = 12345;

        final HubUsersEntity userEntity = hubUsersRepository.save(new HubUsersEntity(userName, null));
        final HipChatConfigEntity hipChatConfEnt = hipChatRepository.save(new HipChatConfigEntity(hipChatApiKey, hipChatRoomId, false, "random"));
        hipChatRelationRepository.save(new HubUserHipChatRelation(userEntity.getId(), hipChatConfEnt.getId()));
        projectVersionRelationRepository.save(new HubUserProjectVersionsRelation(userEntity.getId(), projectName, projectVersionName, Boolean.TRUE));

        final List<NotificationEntity> notificationList = new LinkedList<>();
        // TODO load this from json
        final NotificationEntity applicableNotification = new NotificationEntity(userName, eventKey, new Date(), "POLICY_VIOLATION", projectName, "", projectVersionName, "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        final NotificationEntity nonApplicableNotification = new NotificationEntity(userName, eventKey, new Date(), "POLICY_VIOLATION_CLEARED", projectName, "", projectVersionName, "", "Test Component", "Test Component Version",
                "Test Policy Rule Name", "Test Person", Collections.emptyList());
        notificationList.add(applicableNotification);
        notificationList.add(nonApplicableNotification);

        final List<AbstractChannelEvent> eventsCreated = processor.processNotifications(DigestTypeEnum.REAL_TIME, notificationList);
        assertEquals(0, eventsCreated.size());
    }

}
