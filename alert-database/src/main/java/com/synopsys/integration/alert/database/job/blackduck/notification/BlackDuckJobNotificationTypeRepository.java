package com.synopsys.integration.alert.database.job.blackduck.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackDuckJobNotificationTypeRepository extends JpaRepository<BlackDuckJobNotificationTypeEntity, BlackDuckJobNotificationTypePK> {
}
