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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class CommonDistributionConfigActionsTestIT {
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;
    @Autowired
    private DistributionProjectRepository distributionProjectRepository;

    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    public void cleanup() {
        commonDistributionRepository.deleteAll();
        configuredProjectsRepository.deleteAll();
        distributionProjectRepository.deleteAll();
    }

    @Test
    public void saveAndGetTestIT() throws AlertException {
        final String distributionType = null;
        final String name = "My Config";
        final String frequency = "DAILY";
        final String type = "ALL";
        final String filterByProject = "true";
        final List<String> projectList = Arrays.asList("Project 1", "Project 2", "Project 3");

        final CommonDistributionConfigRestModel commonDistributionConfigRestModel = new CommonDistributionConfigRestModel(null, null, distributionType, name, frequency, type, filterByProject, projectList);
        final CommonDistributionConfigActions commonDistributionConfigActions = new CommonDistributionConfigActions(commonDistributionRepository, configuredProjectsRepository, distributionProjectRepository, objectTransformer);

        final CommonDistributionConfigEntity savedEntity = commonDistributionConfigActions.saveConfig(commonDistributionConfigRestModel);
        assertEquals(distributionType, savedEntity.getDistributionType());
        assertEquals(name, savedEntity.getName());
        assertEquals(frequency, savedEntity.getFrequency());
        assertEquals(type, savedEntity.getNotificationType());
        assertEquals(filterByProject, savedEntity.getFilterByProject().toString());
        assertEquals(projectList.size(), distributionProjectRepository.count());
        assertEquals(projectList.size(), configuredProjectsRepository.count());

        final CommonDistributionConfigRestModel updatedRestModel = objectTransformer.databaseEntityToConfigRestModel(savedEntity, CommonDistributionConfigRestModel.class);
        commonDistributionConfigActions.saveConfig(updatedRestModel);
        assertEquals(projectList.size(), distributionProjectRepository.count());
        assertEquals(projectList.size(), configuredProjectsRepository.count());

        final List<CommonDistributionConfigRestModel> foundRestModels = commonDistributionConfigActions.getConfig(savedEntity.getId());
        assertEquals(1, foundRestModels.size());
        final CommonDistributionConfigRestModel foundRestModel = foundRestModels.get(0);

        assertEquals(savedEntity.getId(), objectTransformer.stringToLong(foundRestModel.getId()));
        assertEquals(savedEntity.getDistributionConfigId(), objectTransformer.stringToLong(foundRestModel.getDistributionConfigId()));
        assertEquals(savedEntity.getDistributionType(), foundRestModel.getDistributionType());
        assertEquals(savedEntity.getFilterByProject(), objectTransformer.stringToBoolean(foundRestModel.getFilterByProject()));
        assertEquals(projectList, foundRestModel.getConfiguredProjects());
    }

}
