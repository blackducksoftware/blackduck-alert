/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import javax.validation.constraints.NotNull;

import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.util.Stringable;

public class ProviderContentType extends Stringable {
    private final String notificationType;
    private final Collection<HierarchicalField> filterableFields;

    public ProviderContentType(@NotNull final String notificationType, @NotNull final Collection<HierarchicalField> filterableFields) {
        this.notificationType = notificationType;
        this.filterableFields = filterableFields;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public boolean hasFilterableFields() {
        return !filterableFields.isEmpty();
    }

    public Collection<HierarchicalField> getFilterableFields() {
        return filterableFields;
    }
}
