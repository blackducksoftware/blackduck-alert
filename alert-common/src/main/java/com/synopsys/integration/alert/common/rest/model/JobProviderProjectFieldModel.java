/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class JobProviderProjectFieldModel extends AlertSerializableModel {
    private String name;
    private String href;
    private Boolean missing;

    public JobProviderProjectFieldModel() {
    }

    public JobProviderProjectFieldModel(String name, String href, Boolean missing) {
        this.name = name;
        this.href = href;
        this.missing = missing;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public Boolean getMissing() {
        return missing;
    }

    public void setMissing(Boolean missing) {
        this.missing = missing;
    }

}
