/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public abstract class ApiAction {

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public FieldModel beforeSaveAction(FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public FieldModel beforeUpdateAction(FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public FieldModel afterUpdateAction(FieldModel previousFieldModel, FieldModel currentFieldModel) throws AlertException {
        return currentFieldModel;
    }

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public FieldModel beforeDeleteAction(FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public void afterDeleteAction(FieldModel fieldModel) throws AlertException {
    }

    /**
     * @throws AlertException Overriding classes may throw this exception
     */
    public FieldModel afterGetAction(FieldModel fieldModel) throws AlertException {
        return fieldModel;
    }

}
