package com.blackduck.integration.alert.database.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationTypeRepository extends JpaRepository<AuthenticationTypeEntity, Long> {
}
