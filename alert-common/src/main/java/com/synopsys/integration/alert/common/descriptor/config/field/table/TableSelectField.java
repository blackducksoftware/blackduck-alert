package com.synopsys.integration.alert.common.descriptor.config.field.table;

import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public abstract class TableSelectField extends ConfigField {
    private boolean paged;
    private boolean searchable;
    private List<TableSelectColumn> columns;

    public TableSelectField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean paged, final boolean searchable) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT.getFieldTypeName(), required, sensitive);
        this.paged = paged;
        this.searchable = searchable;
        columns = createTableColumns();
    }

    public abstract List<TableSelectColumn> createTableColumns();

    public abstract List<Map<String, String>> mapDataToColumns();

    public boolean isPaged() {
        return paged;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public List<TableSelectColumn> getColumns() {
        return columns;
    }
}
