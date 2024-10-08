package com.synopsys.integration.alert.authentication.saml.database.accessor;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SAMLConfigAccessorTest {
    private SAMLConfigAccessor samlConfigAccessor;
    private SAMLConfigModel samlConfigModel;

    @BeforeEach
    void init() {
        samlConfigAccessor = SAMLTestHelper.createTestSAMLConfigAccessor();
        samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder().build();
    }

    @Test
    void configExistsAfterCreateConfiguration() throws AlertConfigurationException {
        assertFalse(samlConfigAccessor.doesConfigurationExist());

        samlConfigAccessor.createConfiguration(samlConfigModel);
        assertTrue(samlConfigAccessor.doesConfigurationExist());
    }

    @Test
    void createConfigurationThrowsOnExistingConfig() throws AlertConfigurationException {
        String createdId = samlConfigAccessor.createConfiguration(samlConfigModel).getId();
        assertTrue(samlConfigAccessor.doesConfigurationExist());

        SAMLConfigModel duplicateCreateModel = new SAMLTestHelper.SAMLConfigModelBuilder().build();
        assertThrows(AlertConfigurationException.class, () -> samlConfigAccessor.createConfiguration(duplicateCreateModel));
        String currentConfigId = samlConfigAccessor.getConfiguration().orElseThrow().getId();
        assertNotEquals(currentConfigId, duplicateCreateModel.getId());
        assertEquals(currentConfigId, createdId);
    }

    @Test
    void getConfigIsEmptyAfterDelete() throws AlertConfigurationException {
        assertTrue(samlConfigAccessor.getConfiguration().isEmpty());

        samlConfigAccessor.createConfiguration(samlConfigModel);
        assertFalse(samlConfigAccessor.getConfiguration().isEmpty());

        samlConfigAccessor.deleteConfiguration();
        assertTrue(samlConfigAccessor.getConfiguration().isEmpty());
    }

    @Test
    void updateConfigReturnsSavedModel() throws AlertConfigurationException {
        boolean updatedEnabled = true;
        String updatedMetadataUrl = "https://www.newmetadataurl.com";

        SAMLConfigModel createdModel = samlConfigAccessor.createConfiguration(samlConfigModel);
        assertNotEquals(updatedEnabled, createdModel.getEnabled());
        assertNotEquals(updatedMetadataUrl, createdModel.getMetadataUrl().orElse(""));

        createdModel.setEnabled(updatedEnabled);
        createdModel.setMetadataUrl(updatedMetadataUrl);
        SAMLConfigModel updatedModel = samlConfigAccessor.updateConfiguration(createdModel);
        assertEquals(updatedEnabled, updatedModel.getEnabled());
        assertEquals(updatedMetadataUrl, updatedModel.getMetadataUrl().orElse(""));
    }
}
