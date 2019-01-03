package com.synopsys.integration.alert.web.controller.metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.web.controller.BaseController;

@RestController
@RequestMapping(BaseController.METADATA_BASE_PATH + DescriptorTypeController.TYPES_PATH)
public class DescriptorTypeController {
    public static final String TYPES_PATH = "/types";

    @GetMapping(TYPES_PATH)
    public DescriptorType[] getTypes() {
        return DescriptorType.values();
    }
}
