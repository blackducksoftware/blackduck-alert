/**
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.oauth;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.JsonString;
import com.google.api.client.util.Key;

public class AzureTokenResponse extends TokenResponse {

    // Azure OAuth returns the expires_in as a string not a numeric value.
    // need to override and create a custom TokenResponse to use the JsonString annotation.
    @Key("expires_in")
    @JsonString
    private Long expiresInSeconds;

    public AzureTokenResponse() {
        super();
    }

    @Override
    public AzureTokenResponse clone() {
        return (AzureTokenResponse) super.clone();
    }
}
