package com.synopsys.integration.alert.channel.jira.cloud.database.accessor.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldPK;
import com.synopsys.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJiraCloudJobCustomFieldRepository extends MockRepositoryContainer<JiraCloudJobCustomFieldPK, JiraCloudJobCustomFieldEntity>
    implements JiraCloudJobCustomFieldRepository {

    private static Function<JiraCloudJobCustomFieldEntity, JiraCloudJobCustomFieldPK> createIDGenerator() {
        return entity -> new JiraCloudJobCustomFieldPK(
            entity.getJobId(),
            entity.getFieldName()
        );
    }

    public MockJiraCloudJobCustomFieldRepository() {
        super(createIDGenerator());
    }

    @Override
    public List<JiraCloudJobCustomFieldEntity> findByJobId(UUID jobId) {
        Map<JiraCloudJobCustomFieldPK, JiraCloudJobCustomFieldEntity> dataMap = getDataMap();
        return dataMap.entrySet()
            .stream()
            .filter(pk -> pk.getKey().getJobId().equals(jobId))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    @Override
    public void bulkDeleteByJobId(UUID jobId) {
        Map<JiraCloudJobCustomFieldPK, JiraCloudJobCustomFieldEntity> dataMap = getDataMap();
        Set<JiraCloudJobCustomFieldPK> customFieldPrimaryKeys = dataMap.keySet()
            .stream()
            .filter(pk -> pk.getJobId().equals(jobId))
            .collect(Collectors.toSet());
        deleteAllById(customFieldPrimaryKeys);
    }
}
