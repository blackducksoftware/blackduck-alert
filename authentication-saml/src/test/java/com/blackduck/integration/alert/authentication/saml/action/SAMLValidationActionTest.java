package com.blackduck.integration.alert.authentication.saml.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.saml.SAMLTestHelper;
import com.blackduck.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.blackduck.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;

class SAMLValidationActionTest {
    private SAMLValidationAction samlValidationAction;
    private SAMLConfigModel validSAMLConfigModel;
    private SAMLConfigModel invalidSAMLConfigModel;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void init() {
        samlValidationAction = new SAMLValidationAction(
            new SAMLConfigurationValidator(SAMLTestHelper.createTempDirFilePersistenceUtil(tempDir)),
            SAMLTestHelper.createAuthorizationManager(),
            new AuthenticationDescriptorKey()
        );

        validSAMLConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder().setMetadataUrl("https://www.metadataurl.com/metadata_url").build();
        // Default builder has blank metadatas which makes it invalid
        invalidSAMLConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder().build();
    }

    @Test
    void validateHasErrorsForInvalidConfigModel() {
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = samlValidationAction.validate(invalidSAMLConfigModel);
        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertFalse(validationResponseModelActionResponse.isError());
        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());
        assertTrue(validationResponseModel.hasErrors());
        assertEquals("There were problems with the configuration", validationResponseModel.getMessage());
    }

    @Test
    void validateHasNoErrorsForValidConfigModel() {
        ActionResponse<ValidationResponseModel> validationResponseModelActionResponse = samlValidationAction.validate(validSAMLConfigModel);
        ValidationResponseModel validationResponseModel = validationResponseModelActionResponse.getContent()
            .orElseThrow(() -> new AssertionError("Expected response content not found"));
        assertFalse(validationResponseModelActionResponse.isError());
        assertEquals(HttpStatus.OK, validationResponseModelActionResponse.getHttpStatus());
        assertFalse(validationResponseModel.hasErrors());
        assertEquals("The configuration is valid", validationResponseModel.getMessage());
    }
}
