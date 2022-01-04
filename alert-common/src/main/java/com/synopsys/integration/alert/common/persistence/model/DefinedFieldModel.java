/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Collection;
import java.util.Collections;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;

public class DefinedFieldModel extends AlertSerializableModel {
    private final String key;
    private final Collection<ConfigContextEnum> contexts;
    private final Boolean sensitive;

    public static DefinedFieldModel createDistributionField(String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.DISTRIBUTION, false);
    }

    public static DefinedFieldModel createDistributionSensitiveField(String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.DISTRIBUTION, true);
    }

    public static DefinedFieldModel createGlobalField(String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.GLOBAL, false);
    }

    public static DefinedFieldModel createGlobalSensitiveField(String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.GLOBAL, true);
    }

    public DefinedFieldModel(String key, ConfigContextEnum context, Boolean sensitive) {
        this.key = key;
        contexts = Collections.singleton(context);
        this.sensitive = sensitive;
    }

    public DefinedFieldModel(String key, Collection<ConfigContextEnum> contexts, Boolean sensitive) {
        this.key = key;
        this.contexts = contexts;
        this.sensitive = sensitive;
    }

    public String getKey() {
        return key;
    }

    public Boolean getSensitive() {
        return sensitive;
    }

    public Collection<ConfigContextEnum> getContexts() {
        return contexts;
    }

}
