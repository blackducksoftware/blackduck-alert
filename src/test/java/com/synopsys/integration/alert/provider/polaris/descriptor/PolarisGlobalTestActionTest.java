package com.synopsys.integration.alert.provider.polaris.descriptor;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProviderKey;
import com.synopsys.integration.alert.provider.polaris.actions.PolarisGlobalTestAction;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.config.FieldValidationAction;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Disabled
public class PolarisGlobalTestActionTest {
    private static final PolarisProviderKey POLARIS_PROVIDER_KEY = new PolarisProviderKey();
    private static final String ERROR_POLARIS_ACCESS_TOKEN = "Invalid Polaris Access Token.";
    private static final String ERROR_POLARIS_TIMEOUT = "Must be an Integer greater than zero (0).";

    private PolarisGlobalUIConfig polarisGlobalUIConfig;

    @BeforeEach
    public void init() {
        EncryptionSettingsValidator encryptionValidator = Mockito.mock(EncryptionSettingsValidator.class);
        Mockito.when(encryptionValidator.apply(Mockito.any(), Mockito.any())).thenReturn(List.of());
        polarisGlobalUIConfig = new PolarisGlobalUIConfig(encryptionValidator);
    }

    @Test
    public void validateConfigWhenValidTest() {
        Map<String, String> fieldErrors = new HashMap<>();
        FieldModel fieldModel = Mockito.mock(FieldModel.class);
        FieldValueModel accessTokenField = Mockito.mock(FieldValueModel.class);
        FieldValueModel timeoutField = Mockito.mock(FieldValueModel.class);
        Mockito.when(fieldModel.getFieldValueModel(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of(accessTokenField));
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of("X".repeat(40)));
        Mockito.when(accessTokenField.hasValues()).thenReturn(true);
        Mockito.when(fieldModel.getFieldValueModel(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of(timeoutField));
        Mockito.when(timeoutField.getValue()).thenReturn(Optional.of("100"));
        Mockito.when(timeoutField.hasValues()).thenReturn(true);
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(polarisGlobalUIConfig.createFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertNull(fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN), "Api token should be populated with valid token");
        assertNull(fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT), "Timeout should be populated with valid timeout");
    }

    @Test
    public void validateConfigWhenInvalidTest() {
        Map<String, String> fieldErrors = new HashMap<>();

        FieldModel fieldModel = Mockito.mock(FieldModel.class);
        FieldValueModel accessTokenField = Mockito.mock(FieldValueModel.class);
        FieldValueModel timeoutField = Mockito.mock(FieldValueModel.class);
        Mockito.when(fieldModel.getFieldValueModel(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of(accessTokenField));
        final String shortTokenString = "too short";
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of(shortTokenString));
        Mockito.when(accessTokenField.getValues()).thenReturn(List.of(shortTokenString));
        Mockito.when(accessTokenField.hasValues()).thenReturn(true);
        Mockito.when(fieldModel.getFieldValueModel(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of(timeoutField));
        final String textTimeout = "invalid integer";
        Mockito.when(timeoutField.getValue()).thenReturn(Optional.of(textTimeout));
        Mockito.when(timeoutField.getValues()).thenReturn(List.of(textTimeout));
        Mockito.when(timeoutField.hasValues()).thenReturn(true);
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(polarisGlobalUIConfig.createFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ERROR_POLARIS_ACCESS_TOKEN, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertTrue(fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT).contains(ERROR_POLARIS_TIMEOUT));

        fieldErrors.clear();
        Mockito.when(fieldModel.getFieldValueModel(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of(accessTokenField));
        final String longTokenString = "too long";
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of(longTokenString.repeat(10)));
        Mockito.when(accessTokenField.getValues()).thenReturn(List.of(longTokenString.repeat(10)));
        Mockito.when(accessTokenField.hasValues()).thenReturn(true);
        Mockito.when(fieldModel.getFieldValueModel(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of(timeoutField));
        final String negativeValue = "-10";
        Mockito.when(timeoutField.getValue()).thenReturn(Optional.of(negativeValue));
        Mockito.when(timeoutField.getValues()).thenReturn(List.of(negativeValue));
        Mockito.when(timeoutField.hasValues()).thenReturn(true);

        fieldValidationAction.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ERROR_POLARIS_ACCESS_TOKEN, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertEquals(ERROR_POLARIS_TIMEOUT, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT));
    }

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testConfigWithRealConnectionTestIT() {
        TestProperties testProperties = new TestProperties();

        String polarisUrl = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_URL);
        String polarisAccessToken = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_ACCESS_TOKEN);
        String polarisTimeout = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_TIMEOUT);

        Assumptions.assumeTrue(StringUtils.isNotBlank(polarisUrl), "The Polaris URL is required for this test to run.");
        Assumptions.assumeTrue(StringUtils.isNotBlank(polarisAccessToken), "The Polaris Access Token is required for this test to run.");
        Assumptions.assumeTrue(StringUtils.isNotBlank(polarisTimeout), "The Polaris Timeout is required for this test to run.");

        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, polarisUrl);
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, polarisAccessToken);
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_TIMEOUT, polarisTimeout);

        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTrustCertificate()).thenReturn(Optional.of(Boolean.TRUE));
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        PolarisProperties polarisProperties = new PolarisProperties(POLARIS_PROVIDER_KEY, alertProperties, null, proxyManager, new Gson());

        PolarisGlobalTestAction actionApi = new PolarisGlobalTestAction(polarisProperties);
        try {
            actionApi.testConfig(null, null, fieldAccessor);
        } catch (Exception e) {
            e.printStackTrace();
            fail("An exception was thrown while testing (seemingly) valid config. " + e.toString());
        }
    }

    @Test
    public void testConfigWithInvalidFieldsTest() {
        PolarisGlobalTestAction actionApi = new PolarisGlobalTestAction(null);

        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, "");

        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        try {
            actionApi.testConfig(null, null, fieldAccessor);
            fail("Expected exception to be thrown");
        } catch (IntegrationException e) {
        }

        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, "good enough to satisfy the check");

        try {
            actionApi.testConfig(null, null, fieldAccessor);
            fail("Expected exception to be thrown");
        } catch (IntegrationException e) {
        }

        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "good enough to satisfy the check");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_TIMEOUT, "");
        try {
            actionApi.testConfig(null, null, fieldAccessor);
            fail("Expected exception to be thrown");
        } catch (IntegrationException e) {
        }
    }

    @Test
    public void testConfigThrowsIOExceptionTest() throws IntegrationException {
        AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = Mockito.mock(AccessTokenPolarisHttpClient.class);
        Mockito.when(accessTokenPolarisHttpClient.attemptAuthentication()).thenThrow(new IntegrationException("Do these exceptions really still happen? Wow!"));

        PolarisServerConfig mockConfig = Mockito.mock(PolarisServerConfig.class);
        Mockito.when(mockConfig.createPolarisHttpClient(Mockito.any(IntLogger.class))).thenReturn(accessTokenPolarisHttpClient);

        PolarisServerConfigBuilder mockBuilder = Mockito.mock(PolarisServerConfigBuilder.class);
        Mockito.when(mockBuilder.validateAndGetBuilderStatus()).thenReturn(new BuilderStatus());
        Mockito.when(mockBuilder.build()).thenReturn(mockConfig);

        PolarisProperties polarisProperties = Mockito.mock(PolarisProperties.class);
        Mockito.when(polarisProperties.createInitialPolarisServerConfigBuilder(Mockito.any(IntLogger.class))).thenReturn(mockBuilder);

        PolarisGlobalTestAction actionApi = new PolarisGlobalTestAction(polarisProperties);

        Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, "good enough to satisfy the check");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_TIMEOUT, "100");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "good enough to satisfy the check");

        FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);
        try {
            actionApi.testConfig(null, null, fieldAccessor);
            fail("Expected wrapped IOException to be thrown");
        } catch (IntegrationException e) {
            assertNotNull(e);
        }
    }

}
