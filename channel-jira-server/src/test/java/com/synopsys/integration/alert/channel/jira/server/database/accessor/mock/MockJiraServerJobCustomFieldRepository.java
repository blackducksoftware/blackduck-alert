package com.synopsys.integration.alert.channel.jira.server.database.accessor.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.channel.jira.server.database.job.custom_field.JiraServerJobCustomFieldEntity;
import com.synopsys.integration.alert.channel.jira.server.database.job.custom_field.JiraServerJobCustomFieldPK;
import com.synopsys.integration.alert.channel.jira.server.database.job.custom_field.JiraServerJobCustomFieldRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJiraServerJobCustomFieldRepository
    extends MockRepositoryContainer<JiraServerJobCustomFieldPK, JiraServerJobCustomFieldEntity>
    implements JiraServerJobCustomFieldRepository {

    private static Function<JiraServerJobCustomFieldEntity, JiraServerJobCustomFieldPK> createIDGenerator() {
        return entity -> new JiraServerJobCustomFieldPK(
            entity.getJobId(),
            entity.getFieldName()
        );
    }

    public MockJiraServerJobCustomFieldRepository() {
        super(createIDGenerator());
    }

    @Override
    public List<JiraServerJobCustomFieldEntity> findByJobId(UUID jobId) {
        Map<JiraServerJobCustomFieldPK, JiraServerJobCustomFieldEntity> dataMap = getDataMap();
        return dataMap.entrySet()
            .stream()
            .filter(pk -> pk.getKey().getJobId().equals(jobId))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    @Override
    public void bulkDeleteByJobId(UUID jobId) {
        Map<JiraServerJobCustomFieldPK, JiraServerJobCustomFieldEntity> dataMap = getDataMap();
        Set<JiraServerJobCustomFieldPK> customFieldPrimaryKeys = dataMap.keySet()
            .stream()
            .filter(pk -> pk.getJobId().equals(jobId))
            .collect(Collectors.toSet());
        deleteAllById(customFieldPrimaryKeys);
    }
}
