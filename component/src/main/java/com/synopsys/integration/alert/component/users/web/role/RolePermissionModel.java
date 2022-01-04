/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.role;

import java.util.Set;

import com.synopsys.integration.alert.common.rest.model.Config;

public class RolePermissionModel extends Config {
    private String roleName;
    private Set<PermissionModel> permissions;

    public RolePermissionModel() {
    }

    public RolePermissionModel(String id, String roleName, Set<PermissionModel> permissions) {
        super(id);
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Set<PermissionModel> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionModel> permissionModels) {
        this.permissions = permissionModels;
    }
}
