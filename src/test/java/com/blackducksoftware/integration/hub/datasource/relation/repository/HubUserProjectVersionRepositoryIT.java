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
package com.blackducksoftware.integration.hub.datasource.relation.repository;

import static org.junit.Assert.assertEquals;

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
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelationPK;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class HubUserProjectVersionRepositoryIT {
    @Autowired
    private HubUserProjectVersionsRepository hubUserProjectVersionsRepository;

    @Test
    public void addUserProjectVersionsTestIT() {
        final Long userConfigId = new Long(1L);
        final String projectName = "A project name with spaces";
        final String projectVersionName = "A project version name with spaces";
        final HubUserProjectVersionsRelation entity = new HubUserProjectVersionsRelation(userConfigId, projectName, projectVersionName);
        hubUserProjectVersionsRepository.save(entity);

        final long count = hubUserProjectVersionsRepository.count();
        assertEquals(1, count);

        final HubUserProjectVersionsRelationPK key = new HubUserProjectVersionsRelationPK();
        key.userConfigId = userConfigId;
        key.projectName = projectName;
        key.projectVersionName = projectVersionName;
        final HubUserProjectVersionsRelation foundEntity = hubUserProjectVersionsRepository.findOne(key);
        assertEquals(projectName, foundEntity.getProjectName());
        assertEquals(projectVersionName, foundEntity.getProjectVersionName());
    }

    @Test
    public void addMultipleUsersWithSameIdTestIT() {
        final Long userConfigId = new Long(1L);
        final String projectName = "A project name with spaces";
        final String projectVersionName1 = "A project version name with spaces";
        final String projectVersionName2 = "other";
        final HubUserProjectVersionsRelation entity1 = new HubUserProjectVersionsRelation(userConfigId, projectName, projectVersionName1);
        final HubUserProjectVersionsRelation entity2 = new HubUserProjectVersionsRelation(userConfigId, projectName, projectVersionName2);
        hubUserProjectVersionsRepository.save(entity1);
        hubUserProjectVersionsRepository.save(entity2);

        final long count = hubUserProjectVersionsRepository.count();
        assertEquals(2, count);
    }

}
