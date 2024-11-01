/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.blackduck.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

/**
 * @deprecated This controller is replaced by the oAuthAuthenticate endpoint of AzureBoardsGlobalConfigController.
 * Deprecated in 7.x, To be removed in 9.0.0.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping(AzureBoardsOAuthFunctionController.AZURE_OAUTH_FUNCTION_URL)
public class AzureBoardsOAuthFunctionController extends AbstractFunctionController<OAuthEndpointResponse> {
    public static final String AZURE_OAUTH_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + AzureBoardsDescriptor.KEY_OAUTH;

    @Autowired
    public AzureBoardsOAuthFunctionController(AzureBoardsCustomFunctionAction functionAction) {
        super(functionAction);
    }
}
