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
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointButtonField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class AzureBoardsGlobalUIConfig extends UIConfig {
    public static final String LABEL_AZURE_BOARDS_URL = "Url";
    public static final String LABEL_ORGANIZATION_NAME = "Organization Name";
    public static final String LABEL_CONSUMER_KEY = "Consumer Key";
    public static final String LABEL_PRIVATE_KEY = "Private Key";
    public static final String LABEL_OAUTH = "Initialize OAuth";
    public static final String LABEL_ACCESS_TOKEN = "Access Token";

    //FIXME assign values for the descriptions
    public static final String DESCRIPTION_AZURE_BOARDS_URL = "FILL OUT THIS DESCRIPTION";
    public static final String DESCRIPTION_ORGANIZATION_NAME = "FILL OUT THIS DESCRIPTION";
    public static final String DESCRIPTION_CONSUMER_KEY = "FILL OUT THIS DESCRIPTION";
    public static final String DESCRIPTION_PRIVATE_KEY = "FILL OUT THIS DESCRIPTION";
    public static final String DESCRIPTION_OAUTH = "FILL OUT THIS DESCRIPTION";
    public static final String DESCRIPTION_ACCESS_TOKEN = "FILL OUT THIS DESCRIPTION";

    public static final String BUTTON_LABEL_OAUTH = "FILL OUT THIS BUTTON LABEL";

    private final EncryptionValidator encryptionValidator;

    @Autowired
    public AzureBoardsGlobalUIConfig(EncryptionValidator encryptionValidator) {
        super(AzureBoardsDescriptor.AZURE_BOARDS_LABEL, AzureBoardsDescriptor.AZURE_BOARDS_DESCRIPTION, AzureBoardsDescriptor.AZURE_BOARDS_URL);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField azureBoardsUrlField = new URLInputConfigField(AzureBoardsDescriptor.KEY_AZURE_BOARDS_URL, LABEL_AZURE_BOARDS_URL, DESCRIPTION_AZURE_BOARDS_URL).applyRequired(true);
        ConfigField organizationName = new TextInputConfigField(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME, LABEL_ORGANIZATION_NAME, DESCRIPTION_ORGANIZATION_NAME).applyRequired(true);
        ConfigField consumerKey = new PasswordConfigField(AzureBoardsDescriptor.KEY_CONSUMER_KEY, LABEL_CONSUMER_KEY, DESCRIPTION_CONSUMER_KEY, encryptionValidator).applyRequired(true);
        ConfigField privateKey = new PasswordConfigField(AzureBoardsDescriptor.KEY_PRIVATE_KEY, LABEL_PRIVATE_KEY, DESCRIPTION_PRIVATE_KEY, encryptionValidator).applyRequired(true);
        ConfigField accessToken = new PasswordConfigField(AzureBoardsDescriptor.KEY_ACCESS_TOKEN, LABEL_ACCESS_TOKEN, DESCRIPTION_ACCESS_TOKEN, encryptionValidator).applyRequired(true);
        ConfigField configureOAuth = new EndpointButtonField(AzureBoardsDescriptor.KEY_OAUTH, LABEL_OAUTH, DESCRIPTION_OAUTH, BUTTON_LABEL_OAUTH)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_AZURE_BOARDS_URL)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_CONSUMER_KEY)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_PRIVATE_KEY)
                                         .applyRequestedDataFieldKey(AzureBoardsDescriptor.KEY_ACCESS_TOKEN);

        return List.of(azureBoardsUrlField, organizationName, consumerKey, privateKey, configureOAuth, accessToken);
    }
}
