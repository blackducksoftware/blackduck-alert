/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.web.AzureOAuthCallbackController;
import com.synopsys.integration.alert.common.AlertProperties;

@Component
public class AzureRedirectUtil {
    private final AlertProperties alertProperties;

    @Autowired
    public AzureRedirectUtil(AlertProperties alertProperties) {
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
        StringBuilder locationBuilder = new StringBuilder(200);
        alertProperties.getServerUrl()
            .ifPresent(locationBuilder::append);
        locationBuilder.append("/channels/");
        locationBuilder.append(AzureBoardsDescriptor.AZURE_BOARDS_URL);
        return locationBuilder.toString();
    }

    /**
     * The OAuth  callback controller URI as a string for Azure to redirect to send the authorization code.
     * This URI string should match the redirect URI in the Azure registered client application.
     * @return The URI string to redirect to from azure when obtaining the authorization code
     */
    public String createOAuthRedirectUri() {
        StringBuilder locationBuilder = new StringBuilder(200);
        alertProperties.getServerUrl()
            .ifPresent(locationBuilder::append);
        locationBuilder.append(AzureOAuthCallbackController.AZURE_OAUTH_CALLBACK_PATH);
        return locationBuilder.toString();
    }
}
