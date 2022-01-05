/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.Map;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;

public class Configuration extends AlertSerializableModel {
    private final FieldUtility fieldUtility;

    public Configuration(Map<String, ConfigurationFieldModel> keyToFieldMap) {
        fieldUtility = new FieldUtility(keyToFieldMap);
    }

    public FieldUtility getFieldUtility() {
        return fieldUtility;
    }

}
