package com.synopsys.integration.alert.authentication.saml.validator;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class SAMLConfigurationValidatorTest {
    private SAMLConfigurationValidator samlConfigurationValidator;

    @Mock
    private FilePersistenceUtil filePersistenceUtil;

    @BeforeEach
    void init() {
        samlConfigurationValidator = new SAMLConfigurationValidator(filePersistenceUtil);
    }

    @Test
    void validateMetadataFileNotUploadedHasErrors() {
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_METADATA_FILE)).thenReturn(false);
        SAMLConfigModel samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataMode(SAMLMetadataMode.FILE)
            .build();

        ValidationResponseModel validationResponseModel = samlConfigurationValidator.validate(samlConfigModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_FILE_MISSING,
            validationResponseModel.getErrors().get("metadataFileName").getFieldMessage());
    }

    @Test
    void validateMetadataModeInvalidUrlHasErrors() {
        SAMLConfigModel samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataMode(SAMLMetadataMode.URL)
            .setMetadataUrl("BAD URL")
            .build();

        ValidationResponseModel validationResponseModel = samlConfigurationValidator.validate(samlConfigModel);
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getErrors().get("metadataUrl").getFieldMessage()
            .contains("no protocol"));
    }

    @Test
    void validateMetadataModeEmptyUrlHasErrors() {
        SAMLConfigModel samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataMode(SAMLMetadataMode.URL)
            .build();

        ValidationResponseModel validationResponseModel = samlConfigurationValidator.validate(samlConfigModel);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals(AuthenticationDescriptor.FIELD_ERROR_SAML_METADATA_URL_MISSING,
            validationResponseModel.getErrors().get("metadataUrl").getFieldMessage());
    }

    @Test
    void validateNoSigningPrivateKeyHasErrors() {
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_CERT_FILE)).thenReturn(true);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE)).thenReturn(false);

        SAMLConfigModel samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataMode(SAMLMetadataMode.URL)
            .setMetadataUrl("https://www.metadataurl.com")
            .build();
        ValidationResponseModel validationResponseModel = samlConfigurationValidator.validate(samlConfigModel);
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(StringUtils.isNotBlank(validationResponseModel.getErrors().get("signingPrivateKeyFileName").getFieldMessage()));
    }

    @Test
    void validateNoEncryptionPrivateKeyHasErrors() {
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE)).thenReturn(true);
        Mockito.when(filePersistenceUtil.uploadFileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE)).thenReturn(false);

        SAMLConfigModel samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataMode(SAMLMetadataMode.URL)
            .setMetadataUrl("https://www.metadataurl.com")
            .build();
        ValidationResponseModel validationResponseModel = samlConfigurationValidator.validate(samlConfigModel);
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(StringUtils.isNotBlank(validationResponseModel.getErrors().get("encryptionPrivateKeyFileName").getFieldMessage()));
    }

    @Test
    void validateValidSAMLConfigHasNoErrors() {
        SAMLConfigModel samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataMode(SAMLMetadataMode.URL)
            .setMetadataUrl("https://www.metadataurl.com")
            .build();
        ValidationResponseModel validationResponseModel = samlConfigurationValidator.validate(samlConfigModel);
        assertFalse(validationResponseModel.hasErrors());
    }
}
