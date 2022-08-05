package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSubTaskRepository extends JpaRepository<JobSubTaskStatusEntity, UUID> {
}
