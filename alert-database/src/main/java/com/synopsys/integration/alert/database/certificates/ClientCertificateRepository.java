package com.synopsys.integration.alert.database.certificates;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCertificateRepository extends JpaRepository<ClientCertificateEntity, UUID> {
}
