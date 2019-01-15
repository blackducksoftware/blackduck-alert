package com.synopsys.integration.alert.database.deprecated.channel.email;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailGroupDistributionRepository extends JpaRepository<EmailGroupDistributionConfigEntity, Long> {
}
