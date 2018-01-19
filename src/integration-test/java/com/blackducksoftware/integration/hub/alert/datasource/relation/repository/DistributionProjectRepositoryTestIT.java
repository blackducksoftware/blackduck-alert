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
package com.blackducksoftware.integration.hub.alert.datasource.relation.repository;

import static org.junit.Assert.assertEquals;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.blackducksoftware.integration.DatabaseSetupRequiredTest;
import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseSetupRequiredTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class DistributionProjectRepositoryTestIT {
    @Autowired
    private DistributionProjectRepositoryWrapper distributionProjectRepository;

    @Test
    public void saveEntityTestIT() {
        final Long distributionConfigId = 1L;
        final Long projectId = 2L;
        final DistributionProjectRelation relation = new DistributionProjectRelation(distributionConfigId, projectId);

        final DistributionProjectRelation savedRelation = distributionProjectRepository.save(relation);
        final long count = distributionProjectRepository.count();
        assertEquals(1, count);

        assertEquals(distributionConfigId, savedRelation.getCommonDistributionConfigId());
        assertEquals(projectId, savedRelation.getProjectId());

    }
}
