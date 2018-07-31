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
package com.blackducksoftware.integration.alert.common.enumeration;

public enum AlertEnvironment {
    //    ALERT_CONFIG_HOME("ALERT_CONFIG_HOME"),
    //    ALERT_TEMPLATES_DIR("ALERT_TEMPLATES_DIR"),
    //    ALERT_IMAGES_DIR("ALERT_IMAGES_DIR"),
    //    HUB_ALWAYS_TRUST_SERVER_CERTIFICATE("HUB_ALWAYS_TRUST_SERVER_CERTIFICATE"),
    //    HUB_PROXY_HOST("HUB_PROXY_HOST"),
    //    HUB_PROXY_PORT("HUB_PROXY_PORT"),
    //    HUB_PROXY_USER("HUB_PROXY_USER"),
    //    HUB_PROXY_PASSWORD("HUB_PROXY_PASSWORD"),
    //    PUBLIC_HUB_WEBSERVER_HOST("PUBLIC_HUB_WEBSERVER_HOST");
    ALERT_CONFIG_HOME("ALERT_CONFIG_HOME"),
    ALERT_TEMPLATES_DIR("ALERT_TEMPLATES_DIR"),
    ALERT_IMAGES_DIR("ALERT_IMAGES_DIR"),
    ALERT_TRUST_SERVER_CERTIFICATE("ALERT_TRUST_SERVER_CERTIFICATE"),
    ALERT_PROXY_HOST("ALERT_PROXY_HOST"),
    ALERT_PROXY_PORT("ALERT_PROXY_PORT"),
    ALERT_PROXY_USER("ALERT_PROXY_USER"),
    ALERT_PROXY_PASSWORD("ALERT_PROXY_PASSWORD"),
    PUBLIC_BLACKDUCK_WEBSERVER_HOST("PUBLIC_BLACKDUCK_WEBSERVER_HOST");

    private final String variableName;

    AlertEnvironment(final String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
