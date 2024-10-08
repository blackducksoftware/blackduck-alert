/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.actions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPManager;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DateUtils;

/**
 * This class should be removed in 8.0.0.
 * @deprecated since 6.13.0
 */
@Deprecated(forRemoval = true, since = "6.13.0")
@Component
public class AuthenticationFieldModelTestAction extends FieldModelTestAction {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationFieldModelTestAction.class);
    private final LDAPManager ldapManager;

    @Autowired
    public AuthenticationFieldModelTestAction(LDAPManager ldapManager) {
        this.ldapManager = ldapManager;
    }

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        logger.info("Testing authentication.");
        boolean ldapEnabled = registeredFieldValues.getBooleanOrFalse(AuthenticationDescriptor.KEY_LDAP_ENABLED);
        boolean samlEnabled = registeredFieldValues.getBooleanOrFalse(AuthenticationDescriptor.KEY_SAML_ENABLED);
        if (!ldapEnabled && !samlEnabled) {
            String errorMessage = "Enable LDAP or SAML authentication.";
            List<AlertFieldStatus> errors = List.of(
                AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage),
                AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENABLED, errorMessage));
            throw new AlertFieldException(errors);
        }

        if (ldapEnabled) {
            performLdapTest(fieldModel, registeredFieldValues);
        }

        return new MessageResult("Successfully tested authentication configuration.");
    }

    private void performLdapTest(FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        logger.info("LDAP enabled testing LDAP authentication.");
        String userName = fieldModel.getFieldValue(AuthenticationDescriptor.TEST_FIELD_KEY_USERNAME).orElse("");
        LDAPConfigModel ldapConfigModel = convertToLDAPConfigModel(registeredFieldValues);
        Optional<LdapAuthenticationProvider> ldapProvider = ldapManager.createAuthProvider(ldapConfigModel);
        String errorMessage = String.format("Ldap Authentication test failed for the test user %s.  Please check the LDAP configuration.", userName);
        List<AlertFieldStatus> errors = new ArrayList<>();
        if (!ldapProvider.isPresent()) {
            errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage));
        } else {
            try {
                Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(
                    userName,
                    fieldModel.getFieldValue(AuthenticationDescriptor.TEST_FIELD_KEY_PASSWORD).orElse("")
                );
                Authentication authentication = ldapProvider.get().authenticate(pendingAuthentication);
                if (!authentication.isAuthenticated()) {
                    errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage));
                }
                authentication.setAuthenticated(false);
            } catch (Exception ex) {
                logger.error("Exception occurred testing LDAP authentication", ex);
                String exceptionMessage = ex.getMessage();
                if (StringUtils.isNotBlank(exceptionMessage)) {
                    errorMessage = String.format("%s Additional details: %s", errorMessage, exceptionMessage);
                }
                errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage));
            }
        }

        if (!errors.isEmpty()) {
            throw new AlertFieldException(errors);
        }
    }

    private LDAPConfigModel convertToLDAPConfigModel(FieldUtility fieldUtility) {
        OffsetDateTime createAndUpdatedDateTime = DateUtils.createCurrentDateTimestamp();
        return new LDAPConfigModel(
            UUID.randomUUID().toString(),
            DateUtils.formatDate(createAndUpdatedDateTime, DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            DateUtils.formatDate(createAndUpdatedDateTime, DateUtils.UTC_DATE_FORMAT_TO_MINUTE),
            fieldUtility.getBooleanOrFalse(AuthenticationDescriptor.KEY_LDAP_ENABLED),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_SERVER),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_MANAGER_DN),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD),
            StringUtils.isNotBlank(fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_MANAGER_PWD)),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_AUTHENTICATION_TYPE),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_REFERRAL),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_BASE),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_SEARCH_FILTER),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_DN_PATTERNS),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_USER_ATTRIBUTES),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_BASE),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_GROUP_SEARCH_FILTER),
            fieldUtility.getStringOrEmpty(AuthenticationDescriptor.KEY_LDAP_GROUP_ROLE_ATTRIBUTE)
        );
    }

}
