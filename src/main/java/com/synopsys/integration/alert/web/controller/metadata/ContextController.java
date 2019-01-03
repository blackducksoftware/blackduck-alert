package com.synopsys.integration.alert.web.controller.metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.web.controller.BaseController;

@RestController
@RequestMapping(BaseController.METADATA_BASE_PATH + ContextController.CONTEXTS_PATH)
public class ContextController {
    public static final String CONTEXTS_PATH = "/contexts";

    @GetMapping(CONTEXTS_PATH)
    public ConfigContextEnum[] getContexts() {
        return ConfigContextEnum.values();
    }
}
