/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.actions;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ApiAction;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

/**
 * @deprecated This class will be removed in 8.0.0.
 */
@Deprecated(forRemoval = true)
@Component
public class AuthenticationApiAction extends ApiAction {

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) {
        return handleNewAndUpdatedConfig(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) {
        return handleNewAndUpdatedConfig(currentFieldModel);
    }

    private FieldModel handleNewAndUpdatedConfig(FieldModel fieldModel) {
        return fieldModel;
    }
}
