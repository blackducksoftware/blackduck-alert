package com.synopsys.integration.alert.channel.jira.cloud.database.accessor.mock;

import java.util.UUID;

import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.JiraCloudJobDetailsRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJiraCloudJobDetailsRepository extends MockRepositoryContainer<UUID, JiraCloudJobDetailsEntity> implements JiraCloudJobDetailsRepository {
    public MockJiraCloudJobDetailsRepository() {
        super(JiraCloudJobDetailsEntity::getJobId);
    }
}
