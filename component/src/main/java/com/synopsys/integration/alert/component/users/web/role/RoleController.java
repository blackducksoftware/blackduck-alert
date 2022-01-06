/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.users.web.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseController;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadAllController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@RestController
@RequestMapping(RoleController.ROLE_BASE_PATH)
public class RoleController extends BaseController implements ReadAllController<MultiRolePermissionModel>, BaseResourceController<RolePermissionModel>, ValidateController<RolePermissionModel> {
    public static final String ROLE_BASE_PATH = AlertRestConstants.CONFIGURATION_PATH + "/role";
    private final RoleActions roleActions;

    @Autowired
    public RoleController(RoleActions roleActions) {
        this.roleActions = roleActions;
    }

    @Override
    public RolePermissionModel create(RolePermissionModel resource) {
        return ResponseFactory.createContentResponseFromAction(roleActions.create(resource));
    }

    @Override
    public RolePermissionModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(roleActions.getOne(id));
    }

    @Override
    public void update(Long id, RolePermissionModel resource) {
        ResponseFactory.createResponseFromAction(roleActions.update(id, resource));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createResponseFromAction(roleActions.delete(id));
    }

    @Override
    public MultiRolePermissionModel getAll() {
        return ResponseFactory.createContentResponseFromAction(roleActions.getAll());
    }

    @Override
    public ValidationResponseModel validate(RolePermissionModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(roleActions.validate(requestBody));
    }
}
