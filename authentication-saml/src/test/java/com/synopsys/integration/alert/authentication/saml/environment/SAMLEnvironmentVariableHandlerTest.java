package com.synopsys.integration.alert.authentication.saml.environment;

import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLMetadataMode;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class SAMLEnvironmentVariableHandlerTest {
    private SAMLConfigModel samlConfigModel;
    private SAMLEnvironmentVariableHandler samlEnvironmentVariableHandler;
    private MockEnvironment mockEnvironment;
    private SAMLConfigAccessor samlConfigAccessor;

    @BeforeEach
    void initEach() {
        mockEnvironment = new MockEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        samlConfigAccessor = SAMLTestHelper.createTestSAMLConfigAccessor();
        samlEnvironmentVariableHandler = new SAMLEnvironmentVariableHandler(
            samlConfigAccessor,
            new SAMLConfigurationValidator(SAMLTestHelper.createFilePersistenceUtil()),
            environmentVariableUtility
        );
        samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder().build();
    }

    @Test
    void configurationMissingCheckIsFalseAfterSave() {
        assertTrue(samlEnvironmentVariableHandler.configurationMissingCheck());

        samlEnvironmentVariableHandler.saveConfiguration(samlConfigModel, null);
        assertFalse(samlEnvironmentVariableHandler.configurationMissingCheck());
    }

    @Test
    void configureModelSetsFieldsFromEnvironment() {
        SAMLConfigModel cleanEnvironmentConfigModel = samlEnvironmentVariableHandler.configureModel();
        assertTrue(cleanEnvironmentConfigModel.getMetadataUrl().isEmpty());
        // Booleans vals defaults to false if empty
        assertFalse(cleanEnvironmentConfigModel.getEnabled());
        assertFalse(cleanEnvironmentConfigModel.getForceAuth());

        mockEnvironment.setProperty(SAMLEnvironmentVariableHandler.SAML_ENABLED_KEY, "true");
        mockEnvironment.setProperty(SAMLEnvironmentVariableHandler.SAML_FORCE_AUTH_KEY, "true");
        mockEnvironment.setProperty(SAMLEnvironmentVariableHandler.SAML_METADATA_URL_KEY, "https://www.metadataurl.com/metadata_url");

        SAMLConfigModel configuredEnvironmentConfigModel = samlEnvironmentVariableHandler.configureModel();
        assertEquals("https://www.metadataurl.com/metadata_url", configuredEnvironmentConfigModel.getMetadataUrl().orElse(""));
        assertTrue(configuredEnvironmentConfigModel.getEnabled());
        assertTrue(configuredEnvironmentConfigModel.getForceAuth());
    }

    @Test
    void validateChecksValidAndInvalidModels() {
        assertTrue(samlEnvironmentVariableHandler.validateConfiguration(samlConfigModel).hasErrors());

        SAMLConfigModel validSAMLConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataUrl("https://www.metadataurl.com/metadata_url")
            .setMetadataMode(SAMLMetadataMode.URL)
            .build();
        assertFalse(samlEnvironmentVariableHandler.validateConfiguration(validSAMLConfigModel).hasErrors());
    }

    @Test
    void buildProcessingResultBuildsFromModel() {
        EnvironmentProcessingResult environmentProcessingResult = samlEnvironmentVariableHandler.buildProcessingResult(samlConfigModel);
        assertEquals(samlConfigModel.getEnabled().toString(), environmentProcessingResult.getVariableValue(SAMLEnvironmentVariableHandler.SAML_ENABLED_KEY).orElseThrow());
        assertEquals(samlConfigModel.getForceAuth().toString(), environmentProcessingResult.getVariableValue(SAMLEnvironmentVariableHandler.SAML_FORCE_AUTH_KEY).orElseThrow());
        assertEquals(samlConfigModel.getMetadataUrl().orElse(""), environmentProcessingResult.getVariableValue(SAMLEnvironmentVariableHandler.SAML_METADATA_URL_KEY).orElseThrow());

        SAMLConfigModel differentSAMLConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setEnabled(true)
            .setMetadataUrl("https://www.metadataurl.com/metadata_url")
            .setMetadataMode(SAMLMetadataMode.URL)
            .build();
        EnvironmentProcessingResult differentProcessingResult = samlEnvironmentVariableHandler.buildProcessingResult(differentSAMLConfigModel);
        assertEquals(differentSAMLConfigModel.getEnabled().toString(), differentProcessingResult.getVariableValue(SAMLEnvironmentVariableHandler.SAML_ENABLED_KEY).orElseThrow());
        assertEquals(differentSAMLConfigModel.getForceAuth().toString(), differentProcessingResult.getVariableValue(SAMLEnvironmentVariableHandler.SAML_FORCE_AUTH_KEY).orElseThrow());
        assertEquals(differentSAMLConfigModel.getMetadataUrl().orElse(""), differentProcessingResult.getVariableValue(SAMLEnvironmentVariableHandler.SAML_METADATA_URL_KEY).orElseThrow());
    }

    @Test
    void saveConfigurationDoesNotPersistOnExistingConfig() {
        samlConfigModel.setMetadataUrl("https://www.default.com");
        samlEnvironmentVariableHandler.saveConfiguration(samlConfigModel, null);
        SAMLConfigModel savedModel = samlConfigAccessor.getConfiguration().orElseThrow();
        assertEquals(samlConfigModel.getMetadataUrl(), savedModel.getMetadataUrl());

        SAMLConfigModel updatedConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder().setMetadataUrl("https://www.updated.com").build();
        samlEnvironmentVariableHandler.saveConfiguration(samlConfigModel, null);
        savedModel = samlConfigAccessor.getConfiguration().orElseThrow();
        assertNotEquals(updatedConfigModel.getMetadataUrl(), savedModel.getMetadataUrl());
    }
}
