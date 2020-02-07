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
package com.synopsys.integration.alert.web.model;

import com.synopsys.integration.alert.common.annotation.SensitiveField;
import com.synopsys.integration.alert.common.rest.model.Config;

public class LoginConfig extends Config {
    private String blackDuckUsername;

    @SensitiveField
    private String blackDuckPassword;

    public LoginConfig() {
    }

    public LoginConfig(final String blackDuckUsername, final String blackDuckPassword) {
        super("1L");
        this.blackDuckUsername = blackDuckUsername;
        this.blackDuckPassword = blackDuckPassword;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

}
