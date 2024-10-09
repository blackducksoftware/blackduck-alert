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
