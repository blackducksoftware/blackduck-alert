/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.model;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

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
