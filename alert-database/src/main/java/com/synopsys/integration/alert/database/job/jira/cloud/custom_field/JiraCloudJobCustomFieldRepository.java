package com.synopsys.integration.alert.database.job.jira.cloud.custom_field;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraCloudJobCustomFieldRepository extends JpaRepository<JiraCloudJobCustomFieldEntity, JiraCloudJobCustomFieldPK> {
    List<JiraCloudJobCustomFieldEntity> findByJobId(UUID jobId);

}
