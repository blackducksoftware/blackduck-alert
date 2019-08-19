package com.synopsys.integration.alert.common.descriptor.config.field.table;

public class TableSelectColumn {
    private String header;
    private boolean isKey;

    public TableSelectColumn(final String header, final boolean isKey) {
        this.header = header;
        this.isKey = isKey;
    }

    public String getHeader() {
        return header;
    }

    public boolean isKey() {
        return isKey;
    }
}
