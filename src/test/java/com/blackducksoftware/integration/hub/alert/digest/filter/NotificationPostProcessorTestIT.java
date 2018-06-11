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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@WebAppConfiguration
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationPostProcessorTestIT {
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
        final DigestTypeEnum configuredDigestType = DigestTypeEnum.REAL_TIME;
        final NotificationModel notificationModel = createNotificationModel();
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

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(notificationModel.getProjectName()));
        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Set<CommonDistributionConfigEntity> applicableConfigs = postProcessor.getApplicableConfigurations(Arrays.asList(config1, config2), notificationModel, configuredDigestType);
        assertTrue(applicableConfigs.contains(config1));
        assertTrue(applicableConfigs.contains(config2));
        assertEquals(2, applicableConfigs.size());
    }

    @Test
    public void doFrequenciesMatchTest() {
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final CommonDistributionConfigEntity config = new CommonDistributionConfigEntity(13L, EmailGroupChannel.COMPONENT_NAME, "Config 1", DigestTypeEnum.DAILY, true);
        final CommonDistributionConfigEntity configOther = new CommonDistributionConfigEntity(13L, EmailGroupChannel.COMPONENT_NAME, "Config 2", null, false);

        assertTrue(postProcessor.doFrequenciesMatch(config, DigestTypeEnum.DAILY));
        assertFalse(postProcessor.doFrequenciesMatch(config, DigestTypeEnum.REAL_TIME));
        assertFalse(postProcessor.doFrequenciesMatch(configOther, DigestTypeEnum.DAILY));
    }

    @Test
    public void filterMatchingNotificationsTest() {
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
        distributionProjectRepository.deleteAll();

        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final NotificationModel notificationModel = createNotificationModel();
        final Long config1Id = 13L;
        final CommonDistributionConfigEntity config1 = new CommonDistributionConfigEntity(config1Id, EmailGroupChannel.COMPONENT_NAME, "Config 1", DigestTypeEnum.REAL_TIME, true);

        config1.setId(config1Id);

        notificationTypeRepository.save(new NotificationTypeEntity(NotificationCategoryEnum.POLICY_VIOLATION));
        final NotificationTypeEntity notificationType = notificationTypeRepository.findAll().get(0);
        final Long notificationTypeId = notificationType.getId();
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config1.getId(), notificationTypeId));

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(notificationModel.getProjectName()));

        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Optional<NotificationModel> filteredNotificationModel = postProcessor.filterMatchingNotificationTypes(config1, notificationModel);

        assertTrue(filteredNotificationModel.isPresent());
        assertEquals(notificationModel, filteredNotificationModel.get());
    }

    @Test
    public void filterUnmatchedByTypeNotificationsTest() {
        notificationTypeRepository.deleteAll();
        distributionNotificationTypeRepository.deleteAll();
        distributionProjectRepository.deleteAll();
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final NotificationModel notificationModel = createNotificationModel();
        final Long config1Id = 13L;
        final CommonDistributionConfigEntity config1 = new CommonDistributionConfigEntity(config1Id, EmailGroupChannel.COMPONENT_NAME, "Config 1", DigestTypeEnum.REAL_TIME, true);
        config1.setId(config1Id);
        notificationTypeRepository.save(new NotificationTypeEntity(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED));
        final NotificationTypeEntity notificationType = notificationTypeRepository.findAll().get(0);
        final Long notificationTypeId = notificationType.getId();
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config1.getId(), notificationTypeId));

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(notificationModel.getProjectName()));
        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Optional<NotificationModel> filteredNotificationModel = postProcessor.filterMatchingNotificationTypes(config1, notificationModel);

        assertFalse(filteredNotificationModel.isPresent());
    }

    private NotificationModel createNotificationModel() {
        final String projectName = "Project Name";
        final String projectVersion = "Project Version";
        final String eventKey = "event key";
        final Date createdAt = Date.from(ZonedDateTime.now().toInstant());
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.POLICY_VIOLATION;
        final String projectUrl = "project url";
        final String projectVersionUrl = "project version url";
        final String componentName = "component name";
        final String componentVersion = "component version";
        final String policyRuleName = "policy rule";
        final String policyRuleUser = "policy user";
        final NotificationEntity notification = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl,
                componentName, componentVersion, policyRuleName, policyRuleUser);

        final Collection<VulnerabilityEntity> vulnerabilities = Collections.emptyList();
        final NotificationModel model = new NotificationModel(notification, vulnerabilities);
        return model;
    }

}
