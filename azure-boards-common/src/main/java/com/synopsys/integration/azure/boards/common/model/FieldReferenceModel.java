/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.model;

public class FieldReferenceModel {
    private String referenceName;
    private String url;

    public FieldReferenceModel() {
        // For serialization
    }

    public FieldReferenceModel(String referenceName, String url) {
        this.referenceName = referenceName;
        this.url = url;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getUrl() {
        return url;
    }

}
