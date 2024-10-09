package com.blackduck.integration.alert.authentication.saml.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.saml.action.SAMLCrudActions;
import com.blackduck.integration.alert.authentication.saml.action.SAMLFileUploadActions;
import com.blackduck.integration.alert.authentication.saml.action.SAMLValidationAction;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;

@RestController
@RequestMapping(AlertRestConstants.SAML_PATH)
public class SAMLConfigController implements StaticUniqueConfigResourceController<SAMLConfigModel>, ValidateController<SAMLConfigModel> {
    public static final String METADATA_FILE_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/metadata";
    public static final String ENCRYPTION_CERT_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/encryption-cert";
    public static final String ENCRYPTION_PRIVATE_KEY_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/encryption-private-key";
    public static final String SIGNING_CERT_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/signing-cert";
    public static final String SIGNING_PRIVATE_KEY_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/signing-private-key";
    public static final String VERIFICATION_CERT_UPLOAD_PATH = "/" + AlertRestConstants.UPLOAD + "/verification-cert";

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

    // SAML_METADATA_FILE
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

    @DeleteMapping(METADATA_FILE_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMetadataFile() {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.fileDelete(AuthenticationDescriptor.SAML_METADATA_FILE)
        );
    }

    // SAML_ENCRYPTION_CERT_FILE
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

    @DeleteMapping(ENCRYPTION_CERT_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEncryptionCertFile() {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.fileDelete(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)
        );
    }

    // SAML_ENCRYPTION_PRIVATE_KEY_FILE
    @GetMapping(ENCRYPTION_PRIVATE_KEY_UPLOAD_PATH)
    public boolean checkEncryptionPrivateKeyFileExists() {
        return ResponseFactory.createContentResponseFromAction(
            fileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)
        );
    }

    @PostMapping(ENCRYPTION_PRIVATE_KEY_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadEncryptionPrivateKeyFile(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.privateKeyFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, file.getResource())
        );
    }

    @DeleteMapping(ENCRYPTION_PRIVATE_KEY_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEncryptionPrivateKeyFile() {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.fileDelete(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)
        );
    }

    // SAML_SIGNING_CERT_FILE
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

    @DeleteMapping(SIGNING_CERT_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSigningCertFile() {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.fileDelete(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)
        );
    }

    // SAML_SIGNING_PRIVATE_KEY_FILE
    @GetMapping(SIGNING_PRIVATE_KEY_UPLOAD_PATH)
    public boolean checkSigningPrivateKeyFileExists() {
        return ResponseFactory.createContentResponseFromAction(
            fileUploadActions.fileExists(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)
        );
    }

    @PostMapping(SIGNING_PRIVATE_KEY_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadSigningPrivateKeyFile(@RequestParam("file") MultipartFile file) {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.privateKeyFileUpload(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE, file.getResource())
        );
    }

    @DeleteMapping(SIGNING_PRIVATE_KEY_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSigningPrivateKeyFile() {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.fileDelete(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)
        );
    }

    // SAML_VERIFICATION_CERT_FILE
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

    @DeleteMapping(VERIFICATION_CERT_UPLOAD_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVerificationCertFile() {
        ResponseFactory.createResponseFromAction(
            fileUploadActions.fileDelete(AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE)
        );
    }
}
