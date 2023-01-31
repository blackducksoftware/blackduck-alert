/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.actions;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;

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
