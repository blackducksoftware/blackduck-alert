/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class TableSelectColumn extends AlertSerializableModel {
    private final String header;
    private final String headerLabel;
    private final boolean isKey;
    private final boolean sortBy;
    private final boolean hidden;

    public static TableSelectColumn visible(String header, String headerLabel, boolean isKey, boolean sortBy) {
        return new TableSelectColumn(header, headerLabel, isKey, sortBy, false);
    }

    public static TableSelectColumn hidden(String header, String headerLabel, boolean isKey, boolean sortBy) {
        return new TableSelectColumn(header, headerLabel, isKey, sortBy, true);
    }

    public TableSelectColumn(String header, String headerLabel, boolean isKey, boolean sortBy, boolean hidden) {
        this.header = header;
        this.headerLabel = headerLabel;
        this.isKey = isKey;
        this.sortBy = sortBy;
        this.hidden = hidden;
    }

    public String getHeader() {
        return header;
    }

    public String getHeaderLabel() {
        return headerLabel;
    }

    public boolean isIsKey() {
        return isKey;
    }

    public boolean isSortBy() {
        return sortBy;
    }

    public boolean isHidden() {
        return hidden;
    }

}
