/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.certificates;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomCertificateRepository extends JpaRepository<CustomCertificateEntity, Long> {
    Optional<CustomCertificateEntity> findByAlias(String alias);
}
