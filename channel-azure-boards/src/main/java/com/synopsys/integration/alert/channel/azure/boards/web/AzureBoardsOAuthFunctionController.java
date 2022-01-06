/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;

@RestController
@RequestMapping(AzureBoardsOAuthFunctionController.AZURE_OAUTH_FUNCTION_URL)
public class AzureBoardsOAuthFunctionController extends AbstractFunctionController<OAuthEndpointResponse> {
    public static final String AZURE_OAUTH_FUNCTION_URL = AbstractFunctionController.API_FUNCTION_URL + "/" + AzureBoardsDescriptor.KEY_OAUTH;

    @Autowired
    public AzureBoardsOAuthFunctionController(AzureBoardsCustomFunctionAction functionAction) {
        super(functionAction);
    }
}
