/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.provider.processing;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(ProcessingTypeFunctionController.PROCESSING_TYPE_FUNCTION_URL)
public class ProcessingTypeFunctionController extends AbstractFunctionController<LabelValueSelectOptions> {
    public static final String PROCESSING_TYPE_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ProviderDescriptor.KEY_PROCESSING_TYPE;

    public ProcessingTypeFunctionController(ProcessingSelectCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
