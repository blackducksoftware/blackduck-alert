package com.synopsys.integration.alert.database.job.blackduck.projects;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackDuckJobProjectRepository extends JpaRepository<BlackDuckJobProjectEntity, BlackDuckJobProjectPK> {
}
