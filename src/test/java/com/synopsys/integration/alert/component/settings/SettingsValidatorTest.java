package com.synopsys.integration.alert.component.settings;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageAccessor;
import com.synopsys.integration.alert.util.OutputLogger;

public class SettingsValidatorTest {
    private OutputLogger outputLogger;

    @BeforeEach
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (outputLogger != null) {
            outputLogger.cleanup();
        }
    }

    @Test
    public void testValidateEncryptionProperties() throws IOException {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        SettingsValidator settingsValidator = new SettingsValidator(encryptionUtility, defaultSystemMessageUtility);
        settingsValidator.validateEncryption();
        Mockito.verify(encryptionUtility).isInitialized();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Not Initialized"));
    }

    @Test
    public void testValidateEncryptionPropertiesSuccess() throws IOException {
        EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        DefaultSystemMessageAccessor defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        SettingsValidator settingsValidator = new SettingsValidator(encryptionUtility, defaultSystemMessageUtility);
        settingsValidator.validateEncryption();
        Mockito.verify(encryptionUtility).isInitialized();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Initialized"));
    }
}
