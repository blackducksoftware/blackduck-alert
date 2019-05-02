package com.synopsys.integration.alert.database.authorization;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTaskRepository extends JpaRepository<UserTaskEntity, Long> {
}
