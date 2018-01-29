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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.blackducksoftware.integration.DatabaseSetupRequiredTest;
import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationTypeEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionNotificationTypeRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseSetupRequiredTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestPropertySource(locations = "classpath:spring-test.properties")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class NotificationPostProcessorTestIT {
    @Autowired
    DistributionProjectRepositoryWrapper distributionProjectRepository;
    @Autowired
    ConfiguredProjectsRepositoryWrapper configuredProjectsRepository;
    @Autowired
    DistributionNotificationTypeRepositoryWrapper distributionNotificationTypeRepository;
    @Autowired
    NotificationTypeRepositoryWrapper notificationTypeRepository;

    @Test
    public void getApplicableConfigurationsTest() {
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        final DigestTypeEnum digestType = DigestTypeEnum.REAL_TIME;
        final String projectName = "Project Name";
        final String projectVersion = "Project Version";
        final List<Long> notificationIds = Collections.emptyList();
        final Map<NotificationCategoryEnum, CategoryData> categoryMap = new HashMap<>();
        for (final NotificationCategoryEnum categoryEnum : NotificationCategoryEnum.values()) {
            categoryMap.put(categoryEnum, new CategoryData(null, null, 0));
            notificationTypeRepository.save(new NotificationTypeEntity(categoryEnum));
        }
        final ProjectData projectData = new ProjectData(digestType, projectName, projectVersion, notificationIds, categoryMap);
        final Long config1Id = 13L;
        final CommonDistributionConfigEntity config1 = new CommonDistributionConfigEntity(config1Id, SupportedChannels.EMAIL_GROUP, "Config 1", digestType, true);
        config1.setId(config1Id);

        final Long config2Id = 17L;
        final CommonDistributionConfigEntity config2 = new CommonDistributionConfigEntity(config2Id, SupportedChannels.EMAIL_GROUP, "Config 2", digestType, false);
        config2.setId(config2Id);

        final Long notificationTypeId = notificationTypeRepository.findAll().get(0).getId();
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config1.getId(), notificationTypeId));
        distributionNotificationTypeRepository.save(new DistributionNotificationTypeRelation(config2.getId(), notificationTypeId));

        final ConfiguredProjectEntity configuredProjectEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
        distributionProjectRepository.save(new DistributionProjectRelation(config1.getId(), configuredProjectEntity.getId()));

        final Set<CommonDistributionConfigEntity> applicableConfigs = postProcessor.getApplicableConfigurations(Arrays.asList(config1, config2), projectData);
        assertTrue(applicableConfigs.contains(config1));
        assertTrue(applicableConfigs.contains(config2));
        assertEquals(2, applicableConfigs.size());
    }

    @Test
    public void isApplicableTest() {

        final DistributionNotificationTypeRepositoryWrapper distributionNotificationTypeRepository = Mockito.mock(DistributionNotificationTypeRepositoryWrapper.class);
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        Mockito.when(distributionNotificationTypeRepository.findByCommonDistributionConfigId(Mockito.anyLong())).thenReturn(Collections.emptyList());

        final ProjectData projectData = new ProjectData(null, null, null, null, null);
        final CommonDistributionConfigEntity config = new CommonDistributionConfigEntity(13L, SupportedChannels.EMAIL_GROUP, "Config 1", DigestTypeEnum.DAILY, true);
        config.setId(13L);

        assertFalse(postProcessor.isApplicable(config, projectData));
    }

    @Test
    public void doFrequenciesMatchTest() {
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);
        final DigestTypeEnum digestType = DigestTypeEnum.DAILY;
        final CommonDistributionConfigEntity config = new CommonDistributionConfigEntity(13L, SupportedChannels.EMAIL_GROUP, "Config 1", digestType, true);
        final CommonDistributionConfigEntity configOther = new CommonDistributionConfigEntity(13L, SupportedChannels.EMAIL_GROUP, "Config 2", null, false);
        final ProjectData projectDataMatching = new ProjectData(digestType, null, null, null, null);
        final ProjectData projectDataOther = new ProjectData(DigestTypeEnum.REAL_TIME, null, null, null, null);

        assertTrue(postProcessor.doFrequenciesMatch(config, projectDataMatching));
        assertFalse(postProcessor.doFrequenciesMatch(config, projectDataOther));
        assertFalse(postProcessor.doFrequenciesMatch(configOther, projectDataOther));
    }

    @Test
    public void doNotificationTypesMatchWithNoneConfiguredTest() {
        final DistributionNotificationTypeRepositoryWrapper distributionNotificationTypeRepository = Mockito.mock(DistributionNotificationTypeRepositoryWrapper.class);
        final NotificationPostProcessor postProcessor = new NotificationPostProcessor(distributionProjectRepository, configuredProjectsRepository, distributionNotificationTypeRepository, notificationTypeRepository);

        Mockito.when(distributionNotificationTypeRepository.findByCommonDistributionConfigId(Mockito.anyLong())).thenReturn(Collections.emptyList());

        final ProjectData projectData = new ProjectData(null, null, null, null, null);
        final CommonDistributionConfigEntity config = new CommonDistributionConfigEntity(13L, SupportedChannels.EMAIL_GROUP, "Config 1", DigestTypeEnum.DAILY, true);
        config.setId(13L);

        assertFalse(postProcessor.doNotificationTypesMatch(config, projectData));
    }

}
