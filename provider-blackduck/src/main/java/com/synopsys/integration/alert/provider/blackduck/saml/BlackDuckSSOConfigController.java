/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.ResponseFactory;

@RestController
@RequestMapping("/api/blackduck/{blackDuckConfigId}/sso/configuration")
public class BlackDuckSSOConfigController {
    private final BlackDuckSSOConfigActions blackDuckSSOConfigActions;

    @Autowired
    public BlackDuckSSOConfigController(BlackDuckSSOConfigActions blackDuckSSOConfigActions) {
        this.blackDuckSSOConfigActions = blackDuckSSOConfigActions;
    }

    @GetMapping
    public BlackDuckSSOConfigResponseModel getBlackDuckSSOConfig(@PathVariable Long blackDuckConfigId) {
        ActionResponse<BlackDuckSSOConfigResponseModel> configActionResponse = blackDuckSSOConfigActions.retrieveBlackDuckSSOConfig(blackDuckConfigId);
        return ResponseFactory.createContentResponseFromAction(configActionResponse);
    }

}
