package com.blackduck.integration.alert.provider.blackduck.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectOptions;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(BlackDuckProjectFunctionController.CHANNEL_CONFIGURED_PROJECT_FUNCTION_URL)
public class BlackDuckProjectFunctionController extends AbstractFunctionController<ProviderProjectOptions> {
    public static final String CHANNEL_CONFIGURED_PROJECT_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + ProviderDescriptor.KEY_CONFIGURED_PROJECT;

    @Autowired
    public BlackDuckProjectFunctionController(BlackDuckProjectCustomFunctionAction functionAction) {
        super(functionAction);
    }

}
