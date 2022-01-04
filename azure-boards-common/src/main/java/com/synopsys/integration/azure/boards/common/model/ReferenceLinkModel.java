/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.model;

public class ReferenceLinkModel {
    private String href;

    public ReferenceLinkModel() {
        // For serialization
    }

    public ReferenceLinkModel(String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

}
