package com.synopsys.integration.alert.database.job.email;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailJobDetailsRepository extends JpaRepository<EmailJobDetailsEntity, UUID> {
}
