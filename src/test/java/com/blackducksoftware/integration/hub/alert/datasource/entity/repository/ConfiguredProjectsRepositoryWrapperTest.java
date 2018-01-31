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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.mock.ProjectMockUtils;

public class ConfiguredProjectsRepositoryWrapperTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testFindByProjectName() throws IOException {
        final ProjectMockUtils projectMockUtils = new ProjectMockUtils();
        final ConfiguredProjectsRepository repository = Mockito.mock(ConfiguredProjectsRepository.class);
        Mockito.when(repository.findByProjectName(Mockito.anyString())).thenReturn(projectMockUtils.getProjectOneEntity());
        final ConfiguredProjectsRepositoryWrapper configuredProjectsRepositoryWrapper = new ConfiguredProjectsRepositoryWrapper(repository) {

            @Override
            public ConfiguredProjectEntity decryptSensitiveData(final ConfiguredProjectEntity entity) throws EncryptionException {
                throw new EncryptionException();
            }
        };

        final ConfiguredProjectEntity actual = configuredProjectsRepositoryWrapper.findByProjectName("any");

        assertNull(actual);
        assertTrue(outputLogger.isLineContainingText("Error finding common distribution config"));
    }
}
