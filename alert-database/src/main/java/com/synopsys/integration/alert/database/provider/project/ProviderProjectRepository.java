/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderProjectRepository extends JpaRepository<ProviderProjectEntity, Long> {
    Optional<ProviderProjectEntity> findFirstByNameAndProviderConfigId(String name, Long providerConfigId);

    List<ProviderProjectEntity> findByProviderConfigId(Long providerConfigId);

    Optional<ProviderProjectEntity> findFirstByHref(String href);

    void deleteByHref(String href);
}
