/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import com.blackducksoftware.integration.hub.alert.annotation.SensitiveField;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public class GlobalHipChatConfigRestModel extends ConfigRestModel {
    private static final long serialVersionUID = 8852683250883814613L;

    @SensitiveField
    private String apiKey;
    private boolean apiKeyIsSet;

    public GlobalHipChatConfigRestModel() {
    }

    public GlobalHipChatConfigRestModel(final String id, final String apiKey, final boolean apiKeyIsSet) {
        super(id);
        this.apiKey = apiKey;
        this.apiKeyIsSet = apiKeyIsSet;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isApiKeyIsSet() {
        return apiKeyIsSet;
    }

}
