/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.database.accessor.mock;

import java.util.UUID;

import com.blackduck.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsEntity;
import com.blackduck.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJiraServerJobDetailsRepository extends MockRepositoryContainer<UUID, JiraServerJobDetailsEntity> implements JiraServerJobDetailsRepository {
    public MockJiraServerJobDetailsRepository() {
        super(JiraServerJobDetailsEntity::getJobId);
    }
}
