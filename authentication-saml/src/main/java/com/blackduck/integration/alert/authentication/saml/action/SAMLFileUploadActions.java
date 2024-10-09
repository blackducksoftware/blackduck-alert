/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.authentication.saml.validator.SAMLFileUploadValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.api.FileUploadHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class SAMLFileUploadActions {
    private final FileUploadHelper fileUploadHelper;
    private final SAMLFileUploadValidator fileUploadValidator;

    @Autowired
    public SAMLFileUploadActions(AuthorizationManager authorizationManager, SAMLFileUploadValidator fileUploadValidator, AuthenticationDescriptorKey authenticationDescriptorKey, FilePersistenceUtil filePersistenceUtil) {
        this.fileUploadHelper = new FileUploadHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey, filePersistenceUtil);
        this.fileUploadValidator = fileUploadValidator;
    }

    public ActionResponse<Boolean> fileExists(String fileName) {
        return fileUploadHelper.fileExists(fileName);
    }

    public ActionResponse<Void> metadataFileUpload(Resource resource) {
        return fileUploadHelper.fileUpload(
            AuthenticationDescriptor.SAML_METADATA_FILE,
            resource,
            () -> fileUploadValidator.validateMetadataFile(resource)
        );
    }

    public ActionResponse<Void> certFileUpload(String certFileName, Resource resource) {
        return fileUploadHelper.fileUpload(
            certFileName,
            resource,
            () -> fileUploadValidator.validateCertFile(certFileName, resource)
        );
    }

    public ActionResponse<Void> privateKeyFileUpload(String keyFileName, Resource resource) {
        return fileUploadHelper.fileUpload(
            keyFileName,
            resource,
            () -> fileUploadValidator.validatePrivateKeyFile(keyFileName, resource)
        );
    }

    public ActionResponse<Void> fileDelete(String fileName) {
        return fileUploadHelper.fileDelete(
            fileName
        );
    }
}
