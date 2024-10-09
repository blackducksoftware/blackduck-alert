/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.security.SAMLManager;
import com.blackduck.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class SAMLCrudActions {
    private final Logger logger = LoggerFactory.getLogger(SAMLCrudActions.class);

    private final ConfigurationCrudHelper configurationCrudHelper;
    private final SAMLConfigAccessor configurationAccessor;
    private final SAMLConfigurationValidator configurationValidator;
    private final SAMLManager samlManager;
    private final FilePersistenceUtil filePersistenceUtil;

    private static final List<String> SAML_FILE_UPLOADS = Arrays.asList(
        AuthenticationDescriptor.SAML_METADATA_FILE,
        AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE,
        AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE,
        AuthenticationDescriptor.SAML_SIGNING_CERT_FILE,
        AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE,
        AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE
    );

    @Autowired
    public SAMLCrudActions(
        AuthorizationManager authorizationManager,
        SAMLConfigAccessor configurationAccessor,
        SAMLConfigurationValidator configurationValidator,
        AuthenticationDescriptorKey authenticationDescriptorKey,
        SAMLManager samlManager,
        FilePersistenceUtil filePersistenceUtil
    ) {
        this.configurationCrudHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.configurationAccessor = configurationAccessor;
        this.configurationValidator = configurationValidator;
        this.samlManager = samlManager;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public ActionResponse<SAMLConfigModel> getOne() {
        return configurationCrudHelper.getOne(
            configurationAccessor::getConfiguration);
    }

    public ActionResponse<SAMLConfigModel> create(SAMLConfigModel resource) {
        ActionResponse<SAMLConfigModel> response = configurationCrudHelper.create(
            () -> configurationValidator.validate(resource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.createConfiguration(resource)
        );

        if (response.isSuccessful()) {
            samlManager.reconfigureSAML();
        }

        return response;
    }

    public ActionResponse<SAMLConfigModel> update(SAMLConfigModel requestResource) {
        ActionResponse<SAMLConfigModel> response = configurationCrudHelper.update(
            () -> configurationValidator.validate(requestResource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.updateConfiguration(requestResource)
        );

        if (response.isSuccessful()) {
            samlManager.reconfigureSAML();
        }

        return response;
    }

    public ActionResponse<SAMLConfigModel> delete() {
        ActionResponse<SAMLConfigModel> response = configurationCrudHelper.delete(
            configurationAccessor::doesConfigurationExist,
            configurationAccessor::deleteConfiguration
        );

        if (response.isSuccessful()) {
            samlManager.reconfigureSAML();

            try {
                for (String fileName : SAML_FILE_UPLOADS) {
                    boolean fileExists = filePersistenceUtil.uploadFileExists(fileName);
                    if (fileExists) {
                        filePersistenceUtil.deleteUploadsFile(fileName);
                    }
                }
            } catch (IOException e) {
                logger.error("Error deleting file during SAML configuration delete caused by: ", e);
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting uploaded file from server during SAML configuration delete.");
            }
        }

        return response;
    }
}
