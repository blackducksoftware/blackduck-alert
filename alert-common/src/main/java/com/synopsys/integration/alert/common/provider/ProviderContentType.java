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
package com.synopsys.integration.alert.common.provider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.util.Stringable;

public class ProviderContentType extends Stringable {
    private final String notificationType;
    private String fontAwesomeIcon;
    private final Collection<JsonField<?>> notificationFields;

    // TODO temporarily pass in the icon until we refactor ContentTypes and allow people to create this mapping elsewhere
    public ProviderContentType(@NotNull final String notificationType, @NotNull final Collection<JsonField<?>> notificationFields, String fontAwesomeIcon) {
        this.notificationType = notificationType;
        this.fontAwesomeIcon = fontAwesomeIcon;
        this.notificationFields = notificationFields;
    }

    public ProviderContentType(final String notificationType, final Collection<JsonField<?>> notificationFields) {
        this(notificationType, notificationFields, null);
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getFontAwesomeIcon() {
        return fontAwesomeIcon;
    }

    public List<JsonField<?>> getNotificationFields() {
        return notificationFields.parallelStream().collect(Collectors.toList());
    }

    public List<JsonField<String>> getFilterableFields() {
        return notificationFields
                   .parallelStream()
                   .filter(field -> !field.getConfigKeyMappings().isEmpty())
                   .filter(field -> field.isOfType(String.class))
                   .map(field -> (JsonField<String>) field)
                   .collect(Collectors.toList());
    }

}
