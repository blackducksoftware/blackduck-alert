/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.security.authentication.saml;

public class AlertWebSSOProfileOptions {
    // TODO enable SAML support
    /*extends WebSSOProfileOptions {
    private static final Logger logger = LoggerFactory.getLogger(AlertWebSSOProfileOptions.class);
    private final SAMLContext samlContext;

    public AlertWebSSOProfileOptions(final SAMLContext samlContext) {
        this.samlContext = samlContext;
    }

    @Override
    public Boolean getForceAuthN() {
        try {
            final ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            return samlContext.getFieldValueBoolean(currentConfiguration, SettingsDescriptor.KEY_SAML_FORCE_AUTH);
        } catch (final AlertDatabaseConstraintException | AlertLDAPConfigurationException e) {
            logger.error("Could not get the SAML force AuthN.", e);
        }
        return false;
    } */

}
