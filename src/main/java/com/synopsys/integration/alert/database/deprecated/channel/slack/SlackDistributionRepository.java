package com.synopsys.integration.alert.database.deprecated.channel.slack;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackDistributionRepository extends JpaRepository<SlackDistributionConfigEntity, Long> {
}
