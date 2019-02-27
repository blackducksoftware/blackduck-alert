package com.synopsys.integration.alert.database.provider.polaris.issue;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PolarisIssueRepository extends JpaRepository<PolarisIssueEntity, Long> {
    Optional<PolarisIssueEntity> findFirstByIssueType(final String issueType);
    List<PolarisIssueEntity> findByProjectId(final Long projectId);
}
