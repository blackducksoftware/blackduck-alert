package com.synopsys.integration.alert.web.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.web.controller.BaseController;

@RestController
@RequestMapping(BaseController.INTERNAL_BASE_PATH + DescriptorController.DESCRIPTORS_PATH)
public class DescriptorController {
    public static final String DESCRIPTORS_PATH = "/descriptors";

    private static final String CONTEXTS_PATH = "/contexts";
    private static final String TYPES_PATH = "/types";

    @GetMapping(CONTEXTS_PATH)
    public ConfigContextEnum[] getContexts() {
        return ConfigContextEnum.values();
    }

    @GetMapping(TYPES_PATH)
    public DescriptorType[] getTypes() {
        return DescriptorType.values();
    }

    // FIXME path variables don't make sense here, these are not resources, these are filters
}
