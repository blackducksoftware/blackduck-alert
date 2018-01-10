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
package com.blackducksoftware.integration.hub.alert.datasource.entity.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.DatabaseSetupRequiredTest;
import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

@Category(DatabaseSetupRequiredTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@Transactional
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class CommonDistributionRepositoryTestIT {
    @Autowired
    private CommonDistributionRepositoryWrapper commonDistributionRepository;

    @Test
    public void saveEntityTestIT() {
        final Long distributionConfigId = 1L;
        final String distributionType = SupportedChannels.EMAIL_GROUP;
        final String name = "My Config";
        final DigestTypeEnum frequency = DigestTypeEnum.DAILY;
        final Boolean filterByProject = Boolean.TRUE;
        final CommonDistributionConfigEntity entity = new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject);
        final CommonDistributionConfigEntity savedEntity = commonDistributionRepository.save(entity);

        assertEquals(1, commonDistributionRepository.count());
        assertNotNull(savedEntity.getId());
        assertEquals(distributionConfigId, savedEntity.getDistributionConfigId());
        assertEquals(distributionType, savedEntity.getDistributionType());
        assertEquals(name, savedEntity.getName());
        assertEquals(frequency, savedEntity.getFrequency());
        assertEquals(filterByProject, savedEntity.getFilterByProject());
    }
}
