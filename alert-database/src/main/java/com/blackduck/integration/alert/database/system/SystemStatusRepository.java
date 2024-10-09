package com.blackduck.integration.alert.database.system;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemStatusRepository extends JpaRepository<SystemStatusEntity, Long> {
}
