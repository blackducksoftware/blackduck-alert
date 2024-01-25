package com.synopsys.integration.alert.database.certificates;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCertificateKeyRepository extends JpaRepository<ClientCertificateKeyEntity, UUID> {
    Optional<ClientCertificateKeyEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
