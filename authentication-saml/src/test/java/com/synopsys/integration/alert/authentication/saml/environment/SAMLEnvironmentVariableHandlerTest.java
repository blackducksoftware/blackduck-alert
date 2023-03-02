package com.synopsys.integration.alert.authentication.saml.environment;

import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.environment.SAMLEnvironmentVariableHandler;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class SAMLEnvironmentVariableHandlerTest {
    private SAMLConfigModel samlConfigModel;
    private SAMLEnvironmentVariableHandler samlEnvironmentVariableHandler;
    private MockEnvironment mockEnvironment;

    @BeforeEach
    void initEach() {
        mockEnvironment = new MockEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(mockEnvironment);
        samlEnvironmentVariableHandler = new SAMLEnvironmentVariableHandler(
            SAMLTestHelper.createTestSAMLConfigAccessor(),
            new SAMLConfigurationValidator(SAMLTestHelper.createFilePersistenceUtil()),
            environmentVariableUtility
        );
        samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder().build();
    }

    @Test
    void configurationMissingCheckIsFalseOnSave() {
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
}
