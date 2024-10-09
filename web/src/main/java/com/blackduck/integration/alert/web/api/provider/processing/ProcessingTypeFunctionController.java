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
