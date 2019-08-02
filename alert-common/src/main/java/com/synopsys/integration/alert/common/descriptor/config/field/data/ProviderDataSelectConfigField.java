package com.synopsys.integration.alert.common.descriptor.config.field.data;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class ProviderDataSelectConfigField extends ProviderDataField {
    private String providerDataEndpoint;
    private boolean searchable = true;
    private boolean multiSelect = false;

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, boolean readOnly, String panel, String header, ConfigValidationFunction validationFunction,
        String providerDataEndpoint, boolean searchable, boolean multiSelect) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, readOnly, panel, header, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
        this.searchable = searchable;
        this.multiSelect = multiSelect;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, boolean readOnly, String panel, String header, ConfigValidationFunction validationFunction,
        String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, readOnly, panel, header, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, panel);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel, ConfigValidationFunction validationFunction, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, panel, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, ConfigValidationFunction validationFunction, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public static ProviderDataSelectConfigField create(String key, String label, String description, String providerDataEndpoint, boolean isMultiSelect) {
        return new ProviderDataSelectConfigField(
            key, label, description, false, false, false, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY, ConfigField.NO_VALIDATION, providerDataEndpoint, true, isMultiSelect);
    }

    public String getProviderDataEndpoint() {
        return providerDataEndpoint;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

}
