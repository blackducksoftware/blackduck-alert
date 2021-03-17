/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.web.AzureOAuthCallbackController;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

@Component
public class AzureRedirectUrlCreator {
    private final AlertProperties alertProperties;

    @Autowired
    public AzureRedirectUrlCreator(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    /**
     * The OAuth callback controller will redirect back to the Alert UI.
     * Only the callback controller should use this method.  All other requests for redirect URIs should use the
     * createOAuthRedirectUri method.
     * @return The url location to redirect to the UI.
     * @see #createOAuthRedirectUri()
     */
    public String createUIRedirectLocation() {
        return alertProperties.getServerUrl("channels", AzureBoardsDescriptor.AZURE_BOARDS_URL)
                   .orElseThrow(() -> new AlertRuntimeException("Could not create the Azure UI Redirect URL."));
    }

    /**
     * The OAuth callback controller URI as a string for Azure to redirect to send the authorization code.
     * This URI string should match the redirect URI in the Azure registered client application.
     * @return The URI string to redirect to from azure when obtaining the authorization code
     */
    public String createOAuthRedirectUri() {
        return alertProperties.getServerUrl(AlertRestConstants.API, AlertRestConstants.CALLBACKS,
            AlertRestConstants.OAUTH, AzureOAuthCallbackController.AZURE)
                   .orElseThrow(() -> new AlertRuntimeException("Could not create the Azure OAuth Redirect URL."));
    }

}
