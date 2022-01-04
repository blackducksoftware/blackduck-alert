/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;

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
