/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.project;

import java.io.Serializable;

public class ProviderUserProjectRelationPK implements Serializable {
    private static final long serialVersionUID = 2978750766498759769L;
    private Long providerUserId;
    private Long providerProjectId;

    public ProviderUserProjectRelationPK() {
        // JPA requires default constructor definitions
    }

    public Long getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(Long providerUserId) {
        this.providerUserId = providerUserId;
    }

    public Long getProviderProjectId() {
        return providerProjectId;
    }

    public void setProviderProjectId(Long providerProjectId) {
        this.providerProjectId = providerProjectId;
    }
}
