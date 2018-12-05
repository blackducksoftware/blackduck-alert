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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ReadOnlyConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckProviderUIConfig extends UIConfig {
    public static final String KEY_BLACKDUCK_URL = "blackduck.url";
    public static final String KEY_BLACKDUCK_API_KEY = "blackduck.api.key";
    public static final String KEY_BLACKDUCK_TIMEOUT = "blackduck.timeout";
    public static final String KEY_BLACKDUCK_PROXY_HOST = "blackduck.proxy.host";
    public static final String KEY_BLACKDUCK_PROXY_PORT = "blackduck.proxy.port";
    public static final String KEY_BLACKDUCK_PROXY_USERNAME = "blackduck.proxy.username";
    public static final String KEY_BLACKDUCK_PROXY_PASSWORD = "blackduck.proxy.password";

    private static final String PROXY_SUB_GROUP = "Proxy Configuration";

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Black Duck", "blackduck", BlackDuckProvider.COMPONENT_NAME, "laptop", setupFields());
    }

    public List<ConfigField> setupFields() {
        final ConfigField blackDuckUrl = new ReadOnlyConfigField(KEY_BLACKDUCK_URL, "Url", true, false);
        final ConfigField blackDuckApiKey = new PasswordConfigField(KEY_BLACKDUCK_API_KEY, "API Token", true);
        final ConfigField blackDuckTimeout = new NumberConfigField(KEY_BLACKDUCK_TIMEOUT, "Timeout", true, false);
        final ConfigField blackDuckProxyHost = new ReadOnlyConfigField(KEY_BLACKDUCK_PROXY_HOST, "Host Name", false, false, PROXY_SUB_GROUP);
        final ConfigField blackDuckProxyPort = new ReadOnlyConfigField(KEY_BLACKDUCK_PROXY_PORT, "Port", false, false, PROXY_SUB_GROUP);
        final ConfigField blackDuckProxyUsername = new ReadOnlyConfigField(KEY_BLACKDUCK_PROXY_USERNAME, "Username", false, false, PROXY_SUB_GROUP);
        final ConfigField blackDuckProxyPassword = new ReadOnlyConfigField(KEY_BLACKDUCK_PROXY_PASSWORD, "ProxyPassword", false, true, PROXY_SUB_GROUP);

        return Arrays.asList(blackDuckUrl, blackDuckApiKey, blackDuckTimeout, blackDuckProxyHost, blackDuckProxyPort, blackDuckProxyUsername, blackDuckProxyPassword);
    }

}
