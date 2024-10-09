package com.blackduck.integration.alert.database.certificates;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomCertificateRepository extends JpaRepository<CustomCertificateEntity, Long> {
    Optional<CustomCertificateEntity> findByAlias(String alias);
}
