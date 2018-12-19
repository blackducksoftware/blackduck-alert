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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.entity.ConfiguredProjectEntity;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class ConfiguredProjectsRepositoryTestIT extends AlertIntegrationTest {
    @Autowired
    private ConfiguredProjectsRepository configuredProjectsRepository;

    @BeforeEach
    public void init() {
        configuredProjectsRepository.deleteAllInBatch();
        configuredProjectsRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        configuredProjectsRepository.deleteAllInBatch();
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
