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
import com.synopsys.integration.alert.database.entity.ConfiguredProjectEntity;

public class ConfiguredProjectsRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;

    @Before
    public void cleanup() {
        configuredProjectsRepository.deleteAll();
    }

    @Test
    public void saveEntityTestIT() {
        final String projectName = "Hub Project";
        final ConfiguredProjectEntity entity = new ConfiguredProjectEntity(projectName);
        final ConfiguredProjectEntity savedEntity = configuredProjectsRepository.save(entity);
        assertEquals(1, configuredProjectsRepository.count());
        assertNotNull(savedEntity.getId());
        assertEquals(projectName, savedEntity.getProjectName());
    }
}
