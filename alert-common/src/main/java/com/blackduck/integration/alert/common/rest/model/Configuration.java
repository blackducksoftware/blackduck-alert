package com.blackduck.integration.alert.common.rest.model;

import java.util.Map;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;

public class Configuration extends AlertSerializableModel {
    private final FieldUtility fieldUtility;

    public Configuration(Map<String, ConfigurationFieldModel> keyToFieldMap) {
        fieldUtility = new FieldUtility(keyToFieldMap);
    }

    public FieldUtility getFieldUtility() {
        return fieldUtility;
    }

}
