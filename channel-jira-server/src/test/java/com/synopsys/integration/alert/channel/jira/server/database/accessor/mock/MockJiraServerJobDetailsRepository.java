package com.synopsys.integration.alert.channel.jira.server.database.accessor.mock;

import java.util.UUID;

import com.synopsys.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJiraServerJobDetailsRepository extends MockRepositoryContainer<UUID, JiraServerJobDetailsEntity> implements JiraServerJobDetailsRepository {
    public MockJiraServerJobDetailsRepository() {
        super(JiraServerJobDetailsEntity::getJobId);
    }
}
