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
package com.synopsys.integration.alert.component.authentication.actions;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationUIConfig;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AuthenticationTestAction extends TestAction {
    private Logger logger = LoggerFactory.getLogger(AuthenticationTestAction.class);
    private LdapManager ldapManager;

    public AuthenticationTestAction(LdapManager ldapManager) {
        this.ldapManager = ldapManager;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        logger.info("Testing authentication.");
        if (registeredFieldValues.getBooleanOrFalse(AuthenticationDescriptor.KEY_LDAP_ENABLED)) {
            Optional<MessageResult> messageResult = performLdapTest(fieldModel, registeredFieldValues);

            if (messageResult.isPresent()) {
                return messageResult.get();
            }
        }

        if (registeredFieldValues.getBooleanOrFalse(AuthenticationDescriptor.KEY_SAML_ENABLED)) {
            Optional<MessageResult> messageResult = performSAMLTest(fieldModel, registeredFieldValues);

            if (messageResult.isPresent()) {
                return messageResult.get();
            }
        }

        return new MessageResult("Successfully tested authentication configuration.");
    }

    private Optional<MessageResult> performLdapTest(FieldModel fieldModel, FieldAccessor registeredFieldValues) throws IntegrationException {
        logger.info("LDAP enabled testing LDAP authentication.");
        String userName = fieldModel.getFieldValue(AuthenticationUIConfig.TEST_FIELD_KEY_USERNAME).orElse("");
        Optional<LdapAuthenticationProvider> ldapProvider = ldapManager.createAuthProvider(registeredFieldValues);
        String errorMessage = String.format("Ldap Authentication test failed for the test user %s.  Please check the LDAP configuration.", userName);
        if (!ldapProvider.isPresent()) {
            return Optional.of(new MessageResult(errorMessage));
        } else {
            Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(userName,
                fieldModel.getFieldValue(AuthenticationUIConfig.TEST_FIELD_KEY_PASSWORD).orElse(""));
            Authentication authentication = ldapProvider.get().authenticate(pendingAuthentication);
            if (!authentication.isAuthenticated()) {
                return Optional.of(new MessageResult(errorMessage));
            }
        }
        return Optional.empty();
    }

    private Optional<MessageResult> performSAMLTest(FieldModel fieldModel, FieldAccessor registeredFieldValues) {
        return Optional.empty();
    }
}
