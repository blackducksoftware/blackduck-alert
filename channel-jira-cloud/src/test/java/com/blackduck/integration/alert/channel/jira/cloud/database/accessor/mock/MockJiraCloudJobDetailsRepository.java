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
