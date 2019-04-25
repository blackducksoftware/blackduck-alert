/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.event;

import com.synopsys.integration.alert.common.rest.model.FieldModel;

public class ConfigurationEvent {
    private final FieldModel fieldModel;
    private final String configurationName;
    private final String context;
    private final ConfigurationEventType eventType;

    public ConfigurationEvent(final FieldModel fieldModel, final String configurationName, final String context, final ConfigurationEventType eventType) {
        this.fieldModel = fieldModel;
        this.configurationName = configurationName;
        this.context = context;
        this.eventType = eventType;
    }

    public ConfigurationEvent(final FieldModel fieldModel, final ConfigurationEventType eventType) {
        this(fieldModel, fieldModel.getDescriptorName(), fieldModel.getContext(), eventType);
    }

    public ConfigurationEvent(final String configurationName, final String context, final ConfigurationEventType eventType) {
        this(null, configurationName, context, eventType);
    }

    public FieldModel getFieldModel() {
        return fieldModel;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public String getContext() {
        return context;
    }

    public ConfigurationEventType getEventType() {
        return eventType;
    }
}
