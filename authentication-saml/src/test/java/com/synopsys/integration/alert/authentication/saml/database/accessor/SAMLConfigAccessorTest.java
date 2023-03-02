package com.synopsys.integration.alert.authentication.saml.database.mock.accessor;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}
