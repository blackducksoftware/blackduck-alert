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
package com.synopsys.integration.alert.database.entity.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;

public class CommonDistributionRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;

    @Before
    public void cleanup() {
        commonDistributionRepository.deleteAll();
    }

    @Test
    public void saveEntityTestIT() {
        final Long distributionConfigId = 1L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String name = "My Config";
        final String providerName = "provider_blackduck";
        final FrequencyType frequency = FrequencyType.DAILY;
        final Boolean filterByProject = Boolean.TRUE;
        final String projectNamePattern = "pattern";
        final FormatType formatType = FormatType.DEFAULT;
        final CommonDistributionConfigEntity entity = new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, providerName, frequency, filterByProject, projectNamePattern, formatType);
        final CommonDistributionConfigEntity savedEntity = commonDistributionRepository.save(entity);

        assertEquals(1, commonDistributionRepository.count());
        assertNotNull(savedEntity.getId());
        assertEquals(distributionConfigId, savedEntity.getDistributionConfigId());
        assertEquals(distributionType, savedEntity.getDistributionType());
        assertEquals(name, savedEntity.getName());
        assertEquals(frequency, savedEntity.getFrequency());
        assertEquals(filterByProject, savedEntity.getFilterByProject());
        assertEquals(projectNamePattern, savedEntity.getProjectNamePattern());
    }
}
