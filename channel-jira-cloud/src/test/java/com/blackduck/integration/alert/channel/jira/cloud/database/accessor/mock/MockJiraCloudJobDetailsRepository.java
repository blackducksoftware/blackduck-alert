/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.database.accessor.mock;

import java.util.UUID;

import com.blackduck.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.blackduck.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJiraCloudJobDetailsRepository extends MockRepositoryContainer<UUID, JiraCloudJobDetailsEntity> implements JiraCloudJobDetailsRepository {
    public MockJiraCloudJobDetailsRepository() {
        super(JiraCloudJobDetailsEntity::getJobId);
    }
}
