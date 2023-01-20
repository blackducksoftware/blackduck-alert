package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.authentication.saml.validator.SAMLFileUploadValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.api.FileUploadHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
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

    public ActionResponse<Boolean> metadataFileExists() {
        return fileUploadHelper.fileExists(AuthenticationDescriptor.SAML_METADATA_FILE);
    }

    public ActionResponse<Void> metadataFileUpload(Resource resource) {
        return fileUploadHelper.fileUpload(
            AuthenticationDescriptor.SAML_METADATA_FILE,
            resource,
            () -> fileUploadValidator.validateMetadataFile(resource)
        );
    }

    public ActionResponse<Boolean> encryptionCertFileExists() {
        return fileUploadHelper.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE);
    }

    public ActionResponse<Void> encryptionCertFileUpload(Resource resource) {
        return fileUploadHelper.fileUpload(
            AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE,
            resource,
            () -> fileUploadValidator.validateEncryptionCertFile(resource)
        );
    }
}
