package com.synopsys.integration.alert.database.certificates;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCertificateKeyRepository extends JpaRepository<ClientCertificateKeyEntity, UUID> {
}
