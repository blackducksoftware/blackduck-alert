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
import java.util.Collection;
import java.util.Map;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class PermissionMatrixModel extends AlertSerializableModel {
    private final Map<PermissionKey, Integer> permissions;

    public PermissionMatrixModel(Map<PermissionKey, Integer> permissions) {
        this.permissions = permissions;
    }

    public Map<PermissionKey, Integer> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(PermissionKey permissionKey, AccessOperation operation) {
        return permissions.containsKey(permissionKey) && operation.isPermitted(permissions.get(permissionKey));
    }

    public boolean hasPermissions(PermissionKey permissionKey) {
        return permissions.containsKey(permissionKey) && (permissions.get(permissionKey) > 0);
    }

    public boolean hasPermissions(PermissionKey permissionKey, AccessOperation... operations) {
        return permissions.containsKey(permissionKey) && Arrays.stream(operations).allMatch(operation -> operation.isPermitted(permissions.get(permissionKey)));
    }

    public boolean anyPermissionMatch(AccessOperation operation, Collection<PermissionKey> permissionKeys) {
        return permissionKeys
                   .stream()
                   .filter(permissions::containsKey)
                   .anyMatch(key -> operation.isPermitted(permissions.get(key)));
    }

    public boolean isReadOnly(PermissionKey permissionKey) {
        if (!permissions.containsKey(permissionKey)) {
            return true;
        }

        Integer operations = permissions.get(permissionKey);
        return AccessOperation.READ.isPermitted(operations) && !AccessOperation.CREATE.isPermitted(operations) && !AccessOperation.WRITE.isPermitted(operations);
    }

    public int getOperations(PermissionKey permissionKey) {
        if (!permissions.containsKey(permissionKey)) {
            return 0;
        }
        return permissions.get(permissionKey);
    }

    public boolean isEmpty() {
        return permissions.isEmpty();
    }

}
