/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(ProviderConfigFunctionController.PROVIDER_CONFIG_NAME_FUNCTION_URL)
public class ProviderConfigFunctionController extends AbstractFunctionController<LabelValueSelectOptions> {
    public static final String PROVIDER_CONFIG_NAME_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ProviderDescriptor.KEY_PROVIDER_CONFIG_ID;

    @Autowired
    public ProviderConfigFunctionController(ProviderConfigSelectCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
