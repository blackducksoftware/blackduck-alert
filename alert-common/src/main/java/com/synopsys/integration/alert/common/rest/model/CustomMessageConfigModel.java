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
package com.synopsys.integration.alert.common.rest.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;

public class CustomMessageConfigModel extends AlertSerializableModel {
    private final String destination;
    private final FieldAccessor fieldAccessor;

    private String customTopic = null;
    private String customMessage = null;
    private String configId = null;

    public CustomMessageConfigModel(final FieldAccessor fieldAccessor) {
        this(fieldAccessor, null);
    }

    public CustomMessageConfigModel(final FieldAccessor fieldAccessor, final String destination) {
        this.fieldAccessor = fieldAccessor;
        this.destination = destination;
    }

    public FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    public Optional<String> getDestination() {
        return Optional.ofNullable(destination);
    }

    public Optional<String> getCustomTopic() {
        return Optional.ofNullable(customTopic);
    }

    public void setCustomTopic(final String customTopic) {
        if (StringUtils.isNotBlank(customTopic)) {
            this.customTopic = customTopic;
        }
    }

    public Optional<String> getCustomMessage() {
        return Optional.ofNullable(customMessage);
    }

    public void setCustomMessage(final String customMessage) {
        if (StringUtils.isNotBlank(customMessage)) {
            this.customMessage = customMessage;
        }
    }

    public Optional<String> getConfigId() {
        return Optional.ofNullable(configId);
    }

    public void setConfigId(final String configId) {
        if (StringUtils.isBlank(configId)) {
            this.configId = null;
        } else {
            this.configId = configId;
        }
    }

}
