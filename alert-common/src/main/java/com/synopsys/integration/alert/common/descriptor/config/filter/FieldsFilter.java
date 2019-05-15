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
package com.synopsys.integration.alert.common.descriptor.config.filter;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class FieldsFilter {
    private final String descriptorName;
    private final ConfigContextEnum context;
    private final AuthorizationManager authorizationManager;

    public FieldsFilter(final String descriptorName, final ConfigContextEnum context, final AuthorizationManager authorizationManager) {
        this.descriptorName = descriptorName;
        this.context = context;
        this.authorizationManager = authorizationManager;
    }

    public final String createPermissionKey() {
        return AuthorizationManager.generateConfigPermissionKey(getContext().name(), getDescriptorName());
    }

    public final String getDescriptorName() {
        return descriptorName;
    }

    public final ConfigContextEnum getContext() {
        return context;
    }

    public final AuthorizationManager getAuthorizationManager() {
        return authorizationManager;
    }

    public final List<ConfigField> filter(final List<ConfigField> fields) {
        final String permissionKey = createPermissionKey();
        if (!authorizationManager.hasPermissions(permissionKey)) {
            return List.of();
        }

        return excludeFields(fields);
    }

    public abstract List<ConfigField> excludeFields(List<ConfigField> fields);

}
