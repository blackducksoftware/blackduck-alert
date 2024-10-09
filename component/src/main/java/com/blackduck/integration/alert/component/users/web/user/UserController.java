/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.users.web.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseResourceController;
import com.blackduck.integration.alert.common.rest.api.ReadAllController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;

@RestController
@RequestMapping(UserController.USER_BASE_PATH)
public class UserController implements ReadAllController<MultiUserConfigResponseModel>, BaseResourceController<UserConfig>, ValidateController<UserConfig> {
    public static final String USER_BASE_PATH = AlertRestConstants.CONFIGURATION_PATH + "/user";

    private final UserActions actions;

    @Autowired
    public UserController(UserActions actions) {
        this.actions = actions;
    }

    @Override
    public UserConfig create(UserConfig resource) {
        return ResponseFactory.createContentResponseFromAction(actions.create(resource));
    }

    @Override
    public UserConfig getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(actions.getOne(id));
    }

    @Override
    public void update(Long id, UserConfig resource) {
        ResponseFactory.createResponseFromAction(actions.update(id, resource));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createResponseFromAction(actions.delete(id));
    }

    @Override
    public MultiUserConfigResponseModel getAll() {
        return ResponseFactory.createContentResponseFromAction(actions.getAll());
    }

    @Override
    public ValidationResponseModel validate(UserConfig requestBody) {
        return ResponseFactory.createContentResponseFromAction(actions.validate(requestBody));
    }
}
