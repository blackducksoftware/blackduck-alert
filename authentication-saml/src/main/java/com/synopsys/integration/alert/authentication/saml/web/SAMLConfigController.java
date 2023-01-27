package com.synopsys.integration.alert.authentication.saml.web;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.action.SAMLCrudActions;
import com.synopsys.integration.alert.authentication.saml.action.SAMLFileUploadActions;
import com.synopsys.integration.alert.authentication.saml.action.SAMLValidationAction;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AlertRestConstants.SAML_PATH)
public class SAMLConfigController implements StaticUniqueConfigResourceController<SAMLConfigModel>, ValidateController<SAMLConfigModel> {
    private static final String METADATA_FILE_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/metadata";
    private static final String ENCRYPTION_CERT_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/encryption-cert";
    private static final String VERIFICATION_CERT_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/verification-cert";
    private static final String SIGNING_CERT_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/signing-cert";

    private final SAMLCrudActions configActions;
    private final SAMLValidationAction validationAction;
    private final SAMLFileUploadActions fileUploadActions;

    @Autowired
    public SAMLConfigController(SAMLCrudActions configActions, SAMLValidationAction validationAction, SAMLFileUploadActions fileUploadActions) {
        this.configActions = configActions;
        this.validationAction = validationAction;
        this.fileUploadActions = fileUploadActions;
    }

    @Override
    public SAMLConfigModel create(SAMLConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public SAMLConfigModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @Override
    public void update(SAMLConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(resource));
    }

    @Override
    public void delete() {
        ResponseFactory.createContentResponseFromAction(configActions.delete());
    }

    @Override
    public ValidationResponseModel validate(SAMLConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }

    @GetMapping(METADATA_FILE_UPLOAD_PATH)
    public boolean checkMetadataFileExists() {
        return ResponseFactory.createContentResponseFromAction(
            fileUploadActions.fileExists(AuthenticationDescriptor.SAML_METADATA_FILE)
        );
    }

    @PostMapping(METADATA_FILE_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadMetadataFile(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.metadataFileUpload(file.getResource())
        );
    }

    @GetMapping(ENCRYPTION_CERT_UPLOAD_PATH)
    public boolean checkEncryptionCertFileExists() {
        return ResponseFactory.createContentResponseFromAction(
            fileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)
        );
    }

    @PostMapping(ENCRYPTION_CERT_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadEncryptionCertFile(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.certFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE, file.getResource())
        );
    }

    @GetMapping(SIGNING_CERT_UPLOAD_PATH)
    public boolean checkSigningCertFileExists() {
        return ResponseFactory.createContentResponseFromAction(
            fileUploadActions.fileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)
        );
    }

    @PostMapping(SIGNING_CERT_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadSigningCertFile(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.certFileUpload(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE, file.getResource())
        );
    }

    @GetMapping(VERIFICATION_CERT_UPLOAD_PATH)
    public boolean checkVerificationCertFileExists() {
        return ResponseFactory.createContentResponseFromAction(
            fileUploadActions.fileExists(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)
        );
    }

    @PostMapping(VERIFICATION_CERT_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadVerificationCertFile(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.certFileUpload(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE, file.getResource())
        );
    }
}
