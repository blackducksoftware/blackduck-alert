/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.role;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.MultiResponseModel;

public class MultiRolePermissionModel extends MultiResponseModel<RolePermissionModel> {
    public MultiRolePermissionModel(final List<RolePermissionModel> models) {
        super(models);
    }

    public List<RolePermissionModel> getRoles() {
        return getModels();
    }

}
