/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.alert.common.digest.filter;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

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

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.database.DatabaseDataSource;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.alert.database.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.database.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.alert.database.entity.repository.NotificationTypeRepository;
import com.blackducksoftware.integration.alert.database.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.alert.database.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.alert.database.relation.repository.DistributionNotificationTypeRepository;
import com.blackducksoftware.integration.alert.database.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationPostProcessorTestIT {
    public static final String PROJECT_NAME = "Project Name";
    @Autowired
    DistributionProjectRepository distributionProjectRepository;
    @Autowired
    ConfiguredProjectsRepository configuredProjectsRepository;
    @Autowired
    DistributionNotificationTypeRepository distributionNotificationTypeRepository;
    @Autowired
    NotificationTypeRepository notificationTypeRepository;

    @Test
    public void getApplicableConfigurationsTest() {

        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
        distributionProjectRepository.deleteAll();

        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);
        final DigestType configuredDigestType = DigestType.REAL_TIME;
        final NotificationContent notificationModel = createNotificationModel();
        final Long config1Id = 13L;

        final CommonDistributionConfigEntity config1 = new CommonDistributionConfigEntity(config1Id, EmailGroupChannel.COMPONENT_NAME, "Config 1", configuredDigestType, true);
        config1.setId(config1Id);

        final Long config2Id = 17L;
        final CommonDistributionConfigEntity config2 = new CommonDistributionConfigEntity(config2Id, EmailGroupChannel.COMPONENT_NAME, "Config 2", configuredDigestType, false);

        config2.setId(config2Id);

        notificationTypeRepository.save(new NotificationTypeEntity(NotificationCategoryEnum.POLICY_VIOLATION));
        final Long notificationTypeId = notificationTypeRepository.findAll().get(0).getId();
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config1.getId(), notificationTypeId));
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config2.getId(), notificationTypeId));

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(PROJECT_NAME));
        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Set<CommonDistributionConfigEntity> applicableConfigs = postProcessor.getApplicableConfigurations(Arrays.asList(config1, config2), notificationModel, configuredDigestType);
        assertTrue(applicableConfigs.contains(config1));
        assertTrue(applicableConfigs.contains(config2));
        assertEquals(2, applicableConfigs.size());
    }

    @Test
    public void doFrequenciesMatchTest() {
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final CommonDistributionConfigEntity config = new CommonDistributionConfigEntity(13L, EmailGroupChannel.COMPONENT_NAME, "Config 1", DigestType.DAILY, true);
        final CommonDistributionConfigEntity configOther = new CommonDistributionConfigEntity(13L, EmailGroupChannel.COMPONENT_NAME, "Config 2", null, false);

        assertTrue(postProcessor.doFrequenciesMatch(config, DigestType.DAILY));
        assertFalse(postProcessor.doFrequenciesMatch(config, DigestType.REAL_TIME));
        assertFalse(postProcessor.doFrequenciesMatch(configOther, DigestType.DAILY));
    }

    @Test
    public void filterMatchingNotificationsTest() {
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
        distributionProjectRepository.deleteAll();

        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final NotificationContent notificationModel = createNotificationModel();
        final Long config1Id = 13L;
        final CommonDistributionConfigEntity config1 = new CommonDistributionConfigEntity(config1Id, EmailGroupChannel.COMPONENT_NAME, "Config 1", DigestType.REAL_TIME, true);

        config1.setId(config1Id);

        notificationTypeRepository.save(new NotificationTypeEntity(NotificationCategoryEnum.POLICY_VIOLATION));
        final NotificationTypeEntity notificationType = notificationTypeRepository.findAll().get(0);
        final Long notificationTypeId = notificationType.getId();
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config1.getId(), notificationTypeId));

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(PROJECT_NAME));

        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Optional<NotificationContent> filteredNotificationModel = postProcessor.filterMatchingNotificationTypes(config1, notificationModel);

        assertTrue(filteredNotificationModel.isPresent());
        assertEquals(notificationModel, filteredNotificationModel.get());
    }

    @Test
    public void filterUnmatchedByTypeNotificationsTest() {
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
        distributionProjectRepository.deleteAll();
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final NotificationContent notificationModel = createNotificationModel();
        final Long config1Id = 13L;
        final CommonDistributionConfigEntity config1 = new CommonDistributionConfigEntity(config1Id, EmailGroupChannel.COMPONENT_NAME, "Config 1", DigestType.REAL_TIME, true);
        config1.setId(config1Id);
        notificationTypeRepository.save(new NotificationTypeEntity(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED));
        final NotificationTypeEntity notificationType = notificationTypeRepository.findAll().get(0);
        final Long notificationTypeId = notificationType.getId();
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config1.getId(), notificationTypeId));

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(PROJECT_NAME));
        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Optional<NotificationContent> filteredNotificationModel = postProcessor.filterMatchingNotificationTypes(config1, notificationModel);

        assertFalse(filteredNotificationModel.isPresent());

    }

    private NotificationContent createNotificationModel() {
        final String projectName = PROJECT_NAME;
        final String projectVersion = "Project Version";
        final Date createdAt = Date.from(ZonedDateTime.now().toInstant());
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.POLICY_VIOLATION;

        return new NotificationContent(createdAt, "provider", notificationType.name(), projectName + projectVersion);
    }

}
