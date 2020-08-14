/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.azure.boards.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointButtonField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class AzureBoardsGlobalUIConfig extends UIConfig {
    public static final String LABEL_AZURE_BOARDS_URL = "Url";
    public static final String LABEL_ORGANIZATION_NAME = "Organization Name";
    public static final String LABEL_CLIENT_ID = "Client Id";
    public static final String LABEL_CLIENT_SECRET = "Client Secret";
    public static final String LABEL_OAUTH = "Microsoft OAuth";

    public static final String DESCRIPTION_AZURE_BOARDS_URL = "If your Azure DevOps instance is \"on-prem\", this field can be used to set that address.";
    public static final String DESCRIPTION_ORGANIZATION_NAME = "The name of the Azure DevOps organization.";
    public static final String DESCRIPTION_CLIENT_ID = "The Client Id created for Alert when registering your Azure DevOps Application.";
    public static final String DESCRIPTION_CLIENT_SECRET = "The Client secret created for Alert when registering your Azure DevOps Application.";
    public static final String DESCRIPTION_OAUTH = "This will redirect you to Microsoft's OAuth login.";

    public static final String BUTTON_LABEL_OAUTH = "Authenticate";

    private final EncryptionValidator encryptionValidator;

    @Autowired
    public AzureBoardsGlobalUIConfig(EncryptionValidator encryptionValidator) {
        super(AzureBoardsDescriptor.AZURE_BOARDS_LABEL, AzureBoardsDescriptor.AZURE_BOARDS_DESCRIPTION, AzureBoardsDescriptor.AZURE_BOARDS_URL);
        this.encryptionValidator = encryptionValidator;
    }

    //FIXME determine which fields will be returned from the OAuth response, and which the user must provide
    @Override
    public List<ConfigField> createFields() {
        //        ConfigField azureBoardsUrlField = new URLInputConfigField(AzureBoardsDescriptor.KEY_AZURE_BOARDS_URL, LABEL_AZURE_BOARDS_URL, DESCRIPTION_AZURE_BOARDS_URL);
        ConfigField organizationName = new TextInputConfigField(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME, LABEL_ORGANIZATION_NAME, DESCRIPTION_ORGANIZATION_NAME).applyRequired(true);
        ConfigField clientId = new TextInputConfigField(AzureBoardsDescriptor.KEY_CLIENT_ID, LABEL_CLIENT_ID, DESCRIPTION_CLIENT_ID).applyRequired(true);
        ConfigField clientSecret = new PasswordConfigField(AzureBoardsDescriptor.KEY_CLIENT_SECRET, LABEL_CLIENT_SECRET, DESCRIPTION_CLIENT_SECRET, encryptionValidator).applyRequired(true);
        ConfigField configureOAuth = new OAuthEndpointButtonField(AzureBoardsDescriptor.KEY_OAUTH, LABEL_OAUTH, DESCRIPTION_OAUTH, BUTTON_LABEL_OAUTH)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_CLIENT_ID);
        // TODO requiredRelatedFields aren't validated when pushing endpoint button fields.
        //.applyRequiredRelatedField(AzureBoardsDescriptor.KEY_CLIENT_ID);
        return List.of(organizationName, clientId, clientSecret, configureOAuth);
    }

}
