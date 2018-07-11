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
package com.blackducksoftware.integration.alert.config;

import org.springframework.stereotype.Component;

@Component
public class AlertEnvironment {
    public static final String ALERT_CONFIG_HOME = "ALERT_CONFIG_HOME";
    public static final String ALERT_TEMPLATES_DIR = "ALERT_TEMPLATES_DIR";
    public static final String ALERT_IMAGES_DIR = "ALERT_IMAGES_DIR";
    public static final String HUB_ALWAYS_TRUST_SERVER_CERTIFICATE = "HUB_ALWAYS_TRUST_SERVER_CERTIFICATE";
    public static final String HUB_PROXY_HOST = "HUB_PROXY_HOST";
    public static final String HUB_PROXY_PORT = "HUB_PROXY_PORT";
    public static final String HUB_PROXY_USER = "HUB_PROXY_USER";
    public static final String HUB_PROXY_PASSWORD = "HUB_PROXY_PASSWORD";
    public static final String PUBLIC_HUB_WEBSERVER_HOST = "PUBLIC_HUB_WEBSERVER_HOST";

    public String getVariable(final String variableName) {
        return System.getenv(variableName);
    }
}
