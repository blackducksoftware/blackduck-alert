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
package com.synopsys.integration.alert.component.authentication.web;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.synopsys.integration.alert.common.rest.model.Config;

public class LoginConfig extends Config {
    private String alertUsername;

    // If this variable name changes be sure to change the value in the toString
    private String alertPassword;

    public LoginConfig() {
    }

    public LoginConfig(String alertUsername, String alertPassword) {
        super("1L");
        this.alertUsername = alertUsername;
        this.alertPassword = alertPassword;
    }

    public String getAlertUsername() {
        return alertUsername;
    }

    public String getAlertPassword() {
        return alertPassword;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE)
                   .setExcludeFieldNames("alertPassword")
                   .toString();
    }
}
