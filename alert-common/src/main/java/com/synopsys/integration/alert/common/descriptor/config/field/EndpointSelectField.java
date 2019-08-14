package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointSelectField extends SelectConfigField {
    private String endpoint;

    public static EndpointSelectField create(final String key, final String label, final String description, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        return new EndpointSelectField(key, label, description, false, searchable, multiSelect, removeSelected, clearable);
    }

    public static EndpointSelectField createRequired(final String key, final String label, final String description, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        return new EndpointSelectField(key, label, description, true, searchable, multiSelect, removeSelected, clearable);
    }

    public EndpointSelectField(final String key, final String label, final String description, final boolean required, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        super(key, label, description, FieldType.ENDPOINT_SELECT, required, searchable, multiSelect, removeSelected, clearable);
        this.endpoint = CustomEndpointManager.CUSTOM_ENDPOINT_URL;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
