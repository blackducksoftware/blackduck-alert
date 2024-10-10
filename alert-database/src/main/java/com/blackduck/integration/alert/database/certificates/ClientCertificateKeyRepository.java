/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.certificates;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCertificateKeyRepository extends JpaRepository<ClientCertificateKeyEntity, UUID> {
    Optional<ClientCertificateKeyEntity> findByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);
}
