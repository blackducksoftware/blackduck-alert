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
package com.synopsys.integration.alert.workflow.startup.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(11)
public class ConfigurationOverridesStartupComponent extends StartupComponent {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationOverridesStartupComponent.class);
    private static final String ENV_VAR_LDAP_DISABLE = "ALERT_LDAP_DISABLED";
    private static final String ENV_VAR_SAML_DISABLE = "ALERT_SAML_DISABLED";
    private static final String ENV_VAR_ADMIN_USER_PASSWORD_RESET = "ALERT_ADMIN_USER_PASSWORD_RESET";

    private EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public ConfigurationOverridesStartupComponent(EnvironmentVariableUtility environmentVariableUtility) {
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    protected void initialize() {
        boolean disableLdap = isEnvironmentVariableActivated(ENV_VAR_LDAP_DISABLE);
        boolean disableSaml = isEnvironmentVariableActivated(ENV_VAR_SAML_DISABLE);
        boolean resetDefaultPassword = isEnvironmentVariableActivated(ENV_VAR_ADMIN_USER_PASSWORD_RESET);
        if (disableLdap) {
            disableLdapAuthentication();
        }

        if (disableSaml) {
            disableSamlAuthentication();
        }

        if (resetDefaultPassword) {
            resetDefaultAdminPassword();
        }
    }

    private boolean isEnvironmentVariableActivated(String environmentVariable) {
        boolean activated = environmentVariableUtility.getEnvironmentValue(environmentVariable)
                                .map(Boolean::valueOf)
                                .orElse(false);
        logger.info("{} = {}", environmentVariable, activated);
        return activated;
    }

    private void disableLdapAuthentication() {

    }

    private void disableSamlAuthentication() {

    }

    private void resetDefaultAdminPassword() {

    }
}
