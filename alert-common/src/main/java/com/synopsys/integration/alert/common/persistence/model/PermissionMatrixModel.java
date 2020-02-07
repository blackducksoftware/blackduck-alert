/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;

public class PermissionMatrixModel {
    private final Map<String, EnumSet<AccessOperation>> permissions;

    public PermissionMatrixModel(final Map<String, EnumSet<AccessOperation>> permissions) {
        this.permissions = permissions;
    }

    public Map<String, EnumSet<AccessOperation>> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(final String permissionKey, final AccessOperation operation) {
        return permissions.containsKey(permissionKey) && permissions.get(permissionKey).contains(operation);
    }

    public boolean hasPermissions(final String permissionKey) {
        return permissions.containsKey(permissionKey) && !permissions.get(permissionKey).isEmpty();
    }

    public boolean anyPermissionMatch(final AccessOperation operation, final String... permissionKeys) {
        return Arrays.stream(permissionKeys).filter(key -> permissions.containsKey(key)).anyMatch(key -> permissions.get(key).contains(operation));
    }

    public boolean isReadOnly(final String permissionKey) {
        if (!permissions.containsKey(permissionKey)) {
            return false;
        }

        EnumSet<AccessOperation> operations = permissions.get(permissionKey);
        return operations.contains(AccessOperation.READ) && !operations.contains(AccessOperation.CREATE) && !operations.contains(AccessOperation.WRITE);
    }

    public Set<AccessOperation> getOperations(final String permissionKey) {
        if (!permissions.containsKey(permissionKey)) {
            return Set.of();
        }
        return Set.copyOf(permissions.get(permissionKey));
    }

    public boolean isEmpty() {
        return permissions.isEmpty();
    }
}
