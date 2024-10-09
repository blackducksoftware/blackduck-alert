package com.blackduck.integration.alert.channel.email.database.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailJobDetailsRepository extends JpaRepository<EmailJobDetailsEntity, UUID> {
}
