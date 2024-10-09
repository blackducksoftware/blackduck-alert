package com.blackduck.integration.alert.channel.azure.boards.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.blackduck.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.blackduck.integration.alert.common.rest.api.AbstractFunctionController;

/**
 * @deprecated This controller is replaced by the oAuthAuthenticate endpoint of AzureBoardsGlobalConfigController.
 * This class should be removed in 8.0.0.
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
