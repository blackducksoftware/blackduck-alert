/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.authentication.security.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

public class AlertWebSSOProfileOptions extends WebSSOProfileOptions {
    private final transient Logger logger = LoggerFactory.getLogger(AlertWebSSOProfileOptions.class);
    private final SAMLContext samlContext;

    public AlertWebSSOProfileOptions(SAMLContext samlContext) {
        this.samlContext = samlContext;
    }

    @Override
    public Boolean getForceAuthN() {
        try {
            ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            return samlContext.getFieldValueBoolean(currentConfiguration, AuthenticationDescriptor.KEY_SAML_FORCE_AUTH);
        } catch (AlertException e) {
            logger.error("Could not get the SAML force Auth.", e);
        }
        return false;
    }

}
