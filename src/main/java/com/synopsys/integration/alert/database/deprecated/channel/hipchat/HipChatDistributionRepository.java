package com.synopsys.integration.alert.database.deprecated.channel.hipchat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HipChatDistributionRepository extends JpaRepository<HipChatDistributionConfigEntity, Long> {
}
