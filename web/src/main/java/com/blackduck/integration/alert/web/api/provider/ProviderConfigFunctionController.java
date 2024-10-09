package com.blackduck.integration.alert.web.api.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(ProviderConfigFunctionController.PROVIDER_CONFIG_NAME_FUNCTION_URL)
public class ProviderConfigFunctionController extends AbstractFunctionController<LabelValueSelectOptions> {
    public static final String PROVIDER_CONFIG_NAME_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ProviderDescriptor.KEY_PROVIDER_CONFIG_ID;

    @Autowired
    public ProviderConfigFunctionController(ProviderConfigSelectCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
