/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
