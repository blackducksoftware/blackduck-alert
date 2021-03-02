/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderUserProjectRelationRepository extends JpaRepository<ProviderUserProjectRelation, ProviderUserProjectRelationPK> {
    List<ProviderUserProjectRelation> findByProviderProjectId(Long providerProjectId);

}
