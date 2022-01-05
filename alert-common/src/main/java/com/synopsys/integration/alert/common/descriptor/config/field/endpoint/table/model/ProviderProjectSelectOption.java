/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ProviderProjectSelectOption extends AlertSerializableModel {
    private final String name;
    private final String href;
    private final String description;

    public ProviderProjectSelectOption(String name, String href, String description) {
        this.name = name;
        this.href = href;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public String getDescription() {
        return description;
    }

}
