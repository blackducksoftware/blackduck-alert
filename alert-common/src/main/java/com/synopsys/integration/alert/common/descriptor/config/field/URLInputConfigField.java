/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class URLInputConfigField extends TextInputConfigField {

    public URLInputConfigField(String key, String label, String description) {
        super(key, label, description);
        applyValidationFunctions(this::validateURL);
    }

    private ValidationResult validateURL(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        String url = fieldValueModel.getValue().orElse("");
        if (StringUtils.isNotBlank(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                return ValidationResult.errors(e.getMessage());
            }
        }

        return ValidationResult.success();
    }
}
