package com.synopsys.integration.alert.database.certificates;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCertificateRepository extends JpaRepository<ClientCertificateEntity, UUID> {
    Optional<ClientCertificateEntity> findByAlias(String alias);

    boolean existsByAlias(String alias);

    void deleteByAlias(String alias);
}
