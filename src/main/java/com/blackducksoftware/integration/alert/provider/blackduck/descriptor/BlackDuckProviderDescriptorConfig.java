/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.provider.blackduck.descriptor;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.descriptor.config.ConfigField;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.common.enumeration.FieldGroup;
import com.blackducksoftware.integration.alert.common.enumeration.FieldType;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class BlackDuckProviderDescriptorConfig extends DescriptorConfig {
    private static final String PROXY_GROUP = "Proxy Configuration";

    @Autowired
    public BlackDuckProviderDescriptorConfig(final BlackDuckTypeConverter databaseContentConverter, final BlackDuckRepositoryAccessor repositoryAccessor, final BlackDuckProviderStartupComponent startupComponent) {
        super(databaseContentConverter, repositoryAccessor, startupComponent);
    }

    @Override
    public UIComponent getUiComponent() {
        final ConfigField blackDuckUrl = new ConfigField("blackduckUrl", "Url", FieldType.READ_ONLY, true, false, FieldGroup.DEFAULT);
        final ConfigField blackDuckApiKey = new ConfigField("blackDuckApiKey", "API Token", FieldType.PASSWORD_INPUT, true, true, FieldGroup.DEFAULT);
        final ConfigField blackDuckTimeout = new ConfigField("blackDuckTimeout", "Timeout", FieldType.NUMBER_INPUT, true, false, FieldGroup.DEFAULT);
        final ConfigField blackDuckProxyHost = new ConfigField("blackDuckProxyHost", "Host Name", FieldType.READ_ONLY, false, false, FieldGroup.DEFAULT, PROXY_GROUP);
        final ConfigField blackDuckProxyPort = new ConfigField("blackDuckProxyPort", "Port", FieldType.READ_ONLY, false, false, FieldGroup.DEFAULT, PROXY_GROUP);
        final ConfigField blackDuckProxyUsername = new ConfigField("blackDuckProxyUsername", "Username", FieldType.READ_ONLY, false, false, FieldGroup.DEFAULT, PROXY_GROUP);
        final ConfigField blackDuckProxyPassword = new ConfigField("blackDuckProxyPassword", "ProxyPassword", FieldType.READ_ONLY, false, true, FieldGroup.DEFAULT, PROXY_GROUP);
        return new UIComponent("Black Duck", "blackduck", "laptop", Arrays.asList(blackDuckUrl, blackDuckApiKey, blackDuckTimeout, blackDuckProxyHost, blackDuckProxyPort, blackDuckProxyUsername, blackDuckProxyPassword));
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {

    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {

    }

}
