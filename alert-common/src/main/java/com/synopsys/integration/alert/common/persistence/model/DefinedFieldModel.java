/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Collection;
import java.util.Collections;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DefinedFieldModel extends AlertSerializableModel {
    private final String key;
    private final Collection<ConfigContextEnum> contexts;
    private final Boolean sensitive;

    public static DefinedFieldModel createDistributionField(final String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.DISTRIBUTION, false);
    }

    public static DefinedFieldModel createDistributionSensitiveField(final String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.DISTRIBUTION, true);
    }

    public static DefinedFieldModel createGlobalField(final String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.GLOBAL, false);
    }

    public static DefinedFieldModel createGlobalSensitiveField(final String key) {
        return new DefinedFieldModel(key, ConfigContextEnum.GLOBAL, true);
    }

    public DefinedFieldModel(final String key, final ConfigContextEnum context, final Boolean sensitive) {
        this.key = key;
        contexts = Collections.singleton(context);
        this.sensitive = sensitive;
    }

    public DefinedFieldModel(final String key, final Collection<ConfigContextEnum> contexts, final Boolean sensitive) {
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
