/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.users.web.role;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.MultiResponseModel;

public class MultiRolePermissionModel extends MultiResponseModel<RolePermissionModel> {
    public MultiRolePermissionModel(final List<RolePermissionModel> models) {
        super(models);
    }

    public List<RolePermissionModel> getRoles() {
        return getModels();
    }

}
