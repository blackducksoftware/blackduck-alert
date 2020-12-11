package com.synopsys.integration.alert.database.job.jira.server.custom_field;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JiraServerJobCustomFieldRepository extends JpaRepository<JiraServerJobCustomFieldEntity, JiraServerJobCustomFieldPK> {
    List<JiraServerJobCustomFieldEntity> findByJobId(UUID jobId);

}
