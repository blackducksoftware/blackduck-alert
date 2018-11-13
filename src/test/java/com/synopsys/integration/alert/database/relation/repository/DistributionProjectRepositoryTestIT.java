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
package com.synopsys.integration.alert.database.relation.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.database.relation.DistributionProjectRelation;

public class DistributionProjectRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private DistributionProjectRepository distributionProjectRepository;

    @Before
    public void cleanup() {
        distributionProjectRepository.deleteAll();
    }

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
