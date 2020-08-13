/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth;

import java.io.Serializable;

import com.synopsys.integration.util.Stringable;

public class OAuthEndpointResponse extends Stringable implements Serializable {
    private final int httpStatus;
    private final boolean authenticated;
    private final String authorizationUrl;
    private final String message;

    // TODO when there is a common response object this should model that object.
    public OAuthEndpointResponse(int httpStatus, boolean authenticated, String authorizationUrl, String message) {
        this.httpStatus = httpStatus;
        this.authenticated = authenticated;
        this.authorizationUrl = authorizationUrl;
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public String getMessage() {
        return message;
    }
}
