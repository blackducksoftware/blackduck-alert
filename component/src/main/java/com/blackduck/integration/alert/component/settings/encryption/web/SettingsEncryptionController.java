/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.encryption.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.ValidateController;
import com.blackduck.integration.alert.component.settings.encryption.action.SettingsEncryptionCrudActions;
import com.blackduck.integration.alert.component.settings.encryption.action.SettingsEncryptionValidationAction;
import com.blackduck.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;

@RestController
@RequestMapping(AlertRestConstants.SETTINGS_ENCRYPTION_PATH)
public class SettingsEncryptionController implements ValidateController<SettingsEncryptionModel> {
    private final SettingsEncryptionCrudActions configActions;
    private final SettingsEncryptionValidationAction validationAction;

    @Autowired
    public SettingsEncryptionController(SettingsEncryptionCrudActions configActions, SettingsEncryptionValidationAction validationAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void create() {
        // Create is not supported for encryption
    }

    @GetMapping
    public SettingsEncryptionModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody SettingsEncryptionModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(resource));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void delete() {
        // Delete is not supported for encryption
    }

    @Override
    public ValidationResponseModel validate(SettingsEncryptionModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }
}
