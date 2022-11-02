package com.synopsys.integration.alert.channel.github.database.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubJobDetailsRepository extends JpaRepository<GitHubJobDetailsEntity, UUID> {
}
