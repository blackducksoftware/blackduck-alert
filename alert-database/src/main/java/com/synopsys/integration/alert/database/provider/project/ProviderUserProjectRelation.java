/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.project;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(ProviderUserProjectRelationPK.class)
@Table(schema = "alert", name = "provider_user_project_relation")
public class ProviderUserProjectRelation extends DatabaseRelation {
    @Id
    @Column(name = "provider_user_id")
    private Long providerUserId;

    @Id
    @Column(name = "provider_project_id")
    private Long providerProjectId;

    public ProviderUserProjectRelation() {
        // JPA requires default constructor definitions
    }

    public ProviderUserProjectRelation(Long providerUserId, Long providerProjectId) {
        super();
        this.providerUserId = providerUserId;
        this.providerProjectId = providerProjectId;
    }

    public Long getProviderUserId() {
        return providerUserId;
    }

    public Long getProviderProjectId() {
        return providerProjectId;
    }
}
