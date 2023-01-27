package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLFileUploadValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.api.FileUploadHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

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
}
