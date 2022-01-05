/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.security.ldap.LdapManager;
import com.synopsys.integration.alert.component.authentication.security.saml.SAMLManager;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class AuthenticationFieldModelTestAction extends FieldModelTestAction {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationFieldModelTestAction.class);
    private final LdapManager ldapManager;
    private final SAMLManager samlManager;

    @Autowired
    public AuthenticationFieldModelTestAction(LdapManager ldapManager, SAMLManager samlManager) {
        this.ldapManager = ldapManager;
        this.samlManager = samlManager;
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

        if (samlEnabled) {
            performSAMLTest(registeredFieldValues);
        }

        return new MessageResult("Successfully tested authentication configuration.");
    }

    private void performLdapTest(FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        logger.info("LDAP enabled testing LDAP authentication.");
        String userName = fieldModel.getFieldValue(AuthenticationDescriptor.TEST_FIELD_KEY_USERNAME).orElse("");
        Optional<LdapAuthenticationProvider> ldapProvider = ldapManager.createAuthProvider(registeredFieldValues);
        String errorMessage = String.format("Ldap Authentication test failed for the test user %s.  Please check the LDAP configuration.", userName);
        List<AlertFieldStatus> errors = new ArrayList<>();
        if (!ldapProvider.isPresent()) {
            errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_LDAP_ENABLED, errorMessage));
        } else {
            try {
                Authentication pendingAuthentication = new UsernamePasswordAuthenticationToken(userName,
                    fieldModel.getFieldValue(AuthenticationDescriptor.TEST_FIELD_KEY_PASSWORD).orElse(""));
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

    private void performSAMLTest(FieldUtility registeredFieldValues) throws IntegrationException {
        List<AlertFieldStatus> errors = new ArrayList<>();
        Optional<String> registeredEntityId = registeredFieldValues.getString(AuthenticationDescriptor.KEY_SAML_ENTITY_ID);
        if (registeredEntityId.isEmpty()) {
            errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_ENTITY_ID, "Entity ID missing."));
        }
        logger.info("Testing SAML Metadata URL...");
        try {
            Optional<ExtendedMetadataDelegate> provider = samlManager.createHttpProvider(registeredFieldValues.getStringOrEmpty(AuthenticationDescriptor.KEY_SAML_METADATA_URL));
            if (provider.isPresent()) {
                ExtendedMetadataDelegate extendedMetadataDelegate = provider.get();
                extendedMetadataDelegate.initialize();
            }
        } catch (Exception ex) {
            logger.error("Testing SAML Metadata URL error: ", ex);
            errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_URL, ex.getMessage()));
        }

        logger.info("Testing SAML Metadata File...");
        try {
            Optional<ExtendedMetadataDelegate> provider = samlManager.createFileProvider();
            if (provider.isPresent()) {
                ExtendedMetadataDelegate extendedMetadataDelegate = provider.get();
                extendedMetadataDelegate.initialize();
            }
        } catch (Exception ex) {
            logger.error("Testing SAML Metadata File error: ", ex);
            errors.add(AlertFieldStatus.error(AuthenticationDescriptor.KEY_SAML_METADATA_FILE, ex.getMessage()));
        }
        samlManager.initializeConfiguration();
        if (!errors.isEmpty()) {
            throw new AlertFieldException(errors);
        }
    }

}
