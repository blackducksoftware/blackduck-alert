package com.synopsys.integration.alert.database.deprecated.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulingRepository extends JpaRepository<SchedulingConfigEntity, Long> {
}
