/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.encryption.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.settings.encryption.action.SettingsEncryptionCrudActions;
import com.synopsys.integration.alert.component.settings.encryption.action.SettingsEncryptionValidationAction;
import com.synopsys.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;

//TODO: Disabled until new UI component is complete
@RestController
@RequestMapping(AlertRestConstants.SETTINGS_ENCRYPTION_PATH)
public class SettingsEncryptionController implements BaseResourceController<SettingsEncryptionModel>, ValidateController<SettingsEncryptionModel> {
    private final SettingsEncryptionCrudActions configActions;
    private final SettingsEncryptionValidationAction validationAction;

    @Autowired
    public SettingsEncryptionController(SettingsEncryptionCrudActions configActions, SettingsEncryptionValidationAction validationAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
    }

    @Override
    public SettingsEncryptionModel create(SettingsEncryptionModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public SettingsEncryptionModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public void update(Long id, SettingsEncryptionModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @Override
    public void delete(Long id) {
        // Delete is not supported for encryption
    }

    @Override
    public ValidationResponseModel validate(SettingsEncryptionModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }
}
