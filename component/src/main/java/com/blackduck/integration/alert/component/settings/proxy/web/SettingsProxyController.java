/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.proxy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.component.settings.proxy.action.SettingsProxyCrudActions;
import com.blackduck.integration.alert.component.settings.proxy.action.SettingsProxyTestAction;
import com.blackduck.integration.alert.component.settings.proxy.action.SettingsProxyValidationAction;

@RestController
@RequestMapping(AlertRestConstants.SETTINGS_PROXY_PATH)
public class SettingsProxyController implements StaticUniqueConfigResourceController<SettingsProxyModel>, ValidateController<SettingsProxyModel> {
    private final SettingsProxyCrudActions configActions;
    private final SettingsProxyValidationAction validationAction;
    private final SettingsProxyTestAction testAction;

    @Autowired
    public SettingsProxyController(SettingsProxyCrudActions configActions, SettingsProxyValidationAction validationAction, SettingsProxyTestAction testAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
        this.testAction = testAction;
    }

    @Override
    public SettingsProxyModel create(SettingsProxyModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public SettingsProxyModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @Override
    public void update(SettingsProxyModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(resource));
    }

    @Override
    public void delete() {
        ResponseFactory.createContentResponseFromAction(configActions.delete());
    }

    @Override
    public ValidationResponseModel validate(SettingsProxyModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestParam String testUrl, @RequestBody SettingsProxyModel resource) {
        return ResponseFactory.createContentResponseFromAction(testAction.testWithPermissionCheck(testUrl, resource));
    }
}
