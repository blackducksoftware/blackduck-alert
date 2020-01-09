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
package com.synopsys.integration.alert.web.security.authentication.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.AuthorizationUtility;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.web.security.authentication.AuthenticationPerformer;
import com.synopsys.integration.alert.web.security.authentication.event.AuthenticationEventManager;

@Component
public class LdapAuthenticationPerformer extends AuthenticationPerformer {
    private final Logger logger = LoggerFactory.getLogger(LdapAuthenticationPerformer.class);

    private LdapManager ldapManager;

    @Autowired
    public LdapAuthenticationPerformer(AuthenticationEventManager authenticationEventManager, AuthorizationUtility authorizationUtility, LdapManager ldapManager) {
        super(authenticationEventManager, authorizationUtility);
        this.ldapManager = ldapManager;
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.LDAP;
    }

    @Override
    public Authentication authenticateWithProvider(Authentication pendingAuthentication) {
        logger.info("Checking ldap based authentication...");
        Authentication result = pendingAuthentication;
        if (ldapManager.isLdapEnabled()) {
            logger.info("LDAP authentication enabled");
            try {
                LdapAuthenticationProvider authenticationProvider = ldapManager.getAuthenticationProvider();
                result = authenticationProvider.authenticate(pendingAuthentication);
            } catch (AlertConfigurationException ex) {
                logger.error("LDAP Configuration error", ex);
            } catch (Exception ex) {
                logger.error("LDAP Authentication error", ex);
            }
        } else {
            logger.info("LDAP authentication disabled");
        }
        return result;
    }

}
