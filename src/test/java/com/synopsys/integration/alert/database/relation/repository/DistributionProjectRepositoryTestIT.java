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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.relation.DistributionProjectRelation;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class DistributionProjectRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private DistributionProjectRepository distributionProjectRepository;

    @BeforeEach
    public void init() {
        distributionProjectRepository.deleteAllInBatch();
        distributionProjectRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        distributionProjectRepository.deleteAllInBatch();
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
