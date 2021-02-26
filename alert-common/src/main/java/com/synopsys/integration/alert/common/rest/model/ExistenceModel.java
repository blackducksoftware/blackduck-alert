/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

public class ExistenceModel extends AlertSerializableModel {
    private Boolean exists;

    public ExistenceModel() {
        // For serialization
    }

    public ExistenceModel(Boolean exists) {
        this.exists = exists;
    }

    public Boolean getExists() {
        return exists;
    }

}
