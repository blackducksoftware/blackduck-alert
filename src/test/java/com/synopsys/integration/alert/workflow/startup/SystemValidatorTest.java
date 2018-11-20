package com.synopsys.integration.alert.workflow.startup;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.OutputLogger;
import com.synopsys.integration.alert.TestAlertProperties;
import com.synopsys.integration.alert.TestBlackDuckProperties;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;

public class SystemValidatorTest {
    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        if (outputLogger != null) {
            outputLogger.cleanup();
        }
    }

    @Test
    public void testValidate() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        final SystemValidator spiedSystemValidator = Mockito.spy(systemValidator);

        spiedSystemValidator.validate();
        Mockito.verify(systemStatusUtility).setSystemInitialized(Mockito.anyBoolean());
    }

    @Test
    public void testValidateEncryptionProperties() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        systemValidator.validateEncryptionProperties();
        Mockito.verify(encryptionUtility).isInitialized();
        Mockito.verify(encryptionUtility).checkForErrors();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Not Initialized"));
    }

    @Test
    public void testValidateEncryptionPropertiesSuccess() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        Mockito.when(encryptionUtility.isInitialized()).thenReturn(true);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        systemValidator.validateEncryptionProperties();
        Mockito.verify(encryptionUtility).isInitialized();
        assertTrue(outputLogger.isLineContainingText("Encryption utilities: Initialized"));
    }

    @Test
    public void testValidateProviders() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        systemValidator.validateProviders();
        assertTrue(outputLogger.isLineContainingText("Validating configured providers: "));
    }

    @Test
    public void testvalidateBlackDuckProviderNullURL() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        testGlobalProperties.setBlackDuckUrl(null);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Invalid; cause: Black Duck URL missing..."));
    }

    @Test
    public void testvalidateBlackDuckProviderLocalhostURL() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to"));
    }

    @Test
    public void testvalidateBlackDuckProviderHubWebserverEnvironmentSet() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(Mockito.mock(GlobalBlackDuckRepository.class), testAlertProperties);
        final TestBlackDuckProperties spiedGlobalProperties = Mockito.spy(testGlobalProperties);
        spiedGlobalProperties.setBlackDuckUrl("https://localhost:443");
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, spiedGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to"));
    }

    @Test
    public void testValidateHubInvalidProvider() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        testGlobalProperties.setBlackDuckUrl("https://localhost:443");
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Using localhost..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Invalid; cause:"));
    }

    @Test
    public void testValidateHubValidProvider() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("Validating BlackDuck Provider..."));
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Valid!"));
    }

    @Test
    public void testValidateHubValidProviderWithProxy() throws IOException {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertProxyHost("google.com");
        testAlertProperties.setAlertProxyPort("3218");
        testAlertProperties.setAlertProxyUsername("AUser");
        testAlertProperties.setAlertProxyPassword("aPassword");
        final TestBlackDuckProperties testGlobalProperties = new TestBlackDuckProperties(testAlertProperties);
        final EncryptionUtility encryptionUtility = Mockito.mock(EncryptionUtility.class);
        final SystemStatusUtility systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        final SystemMessageUtility systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        final SystemValidator systemValidator = new SystemValidator(testAlertProperties, testGlobalProperties, encryptionUtility, systemStatusUtility, systemMessageUtility);
        systemValidator.validateBlackDuckProvider();
        assertTrue(outputLogger.isLineContainingText("BlackDuck Provider Invalid; cause:"));
    }
}
