package com.synopsys.integration.alert.provider.polaris.descriptor;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class PolarisGlobalDescriptorActionApiTest {
    private static final String ERROR_POLARIS_ACCESS_TOKEN = "Invalid Polaris Access Token.";
    private static final String ERROR_POLARIS_TIMEOUT = "Must be an Integer greater than zero (0).";

    private final PolarisGlobalUIConfig polarisGlobalUIConfig = new PolarisGlobalUIConfig();

    @Test
    public void validateConfigWhenValidTest() throws Exception {
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null);

        final Map<String, String> fieldErrors = new HashMap<>();
        final FieldModel fieldModel = Mockito.mock(FieldModel.class);
        final FieldValueModel accessTokenField = Mockito.mock(FieldValueModel.class);
        final FieldValueModel timeoutField = Mockito.mock(FieldValueModel.class);
        Mockito.when(fieldModel.getField(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of(accessTokenField));
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of("X".repeat(40)));
        Mockito.when(accessTokenField.hasValues()).thenReturn(true);
        Mockito.when(fieldModel.getField(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of(timeoutField));
        Mockito.when(timeoutField.getValue()).thenReturn(Optional.of("100"));
        Mockito.when(timeoutField.hasValues()).thenReturn(true);
        final Map<String, ConfigField> configFieldMap = polarisGlobalUIConfig.createFields()
                                                            .stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        actionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT));
    }

    @Test
    public void validateConfigWhenInvalidTest() throws Exception {
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null);

        final Map<String, String> fieldErrors = new HashMap<>();

        final FieldModel fieldModel = Mockito.mock(FieldModel.class);
        final FieldValueModel accessTokenField = Mockito.mock(FieldValueModel.class);
        final FieldValueModel timeoutField = Mockito.mock(FieldValueModel.class);
        Mockito.when(fieldModel.getField(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of(accessTokenField));
        final String shortTokenString = "too short";
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of(shortTokenString));
        Mockito.when(accessTokenField.getValues()).thenReturn(List.of(shortTokenString));
        Mockito.when(accessTokenField.hasValues()).thenReturn(true);
        Mockito.when(fieldModel.getField(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of(timeoutField));
        final String textTimeout = "invalid integer";
        Mockito.when(timeoutField.getValue()).thenReturn(Optional.of(textTimeout));
        Mockito.when(timeoutField.getValues()).thenReturn(List.of(textTimeout));
        Mockito.when(timeoutField.hasValues()).thenReturn(true);
        final Map<String, ConfigField> configFieldMap = polarisGlobalUIConfig.createFields()
                                                            .stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));

        actionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ERROR_POLARIS_ACCESS_TOKEN, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertTrue(fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT).contains(ERROR_POLARIS_TIMEOUT));

        fieldErrors.clear();
        Mockito.when(fieldModel.getField(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of(accessTokenField));
        final String longTokenString = "too long";
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of(longTokenString.repeat(10)));
        Mockito.when(accessTokenField.getValues()).thenReturn(List.of(longTokenString.repeat(10)));
        Mockito.when(accessTokenField.hasValues()).thenReturn(true);
        Mockito.when(fieldModel.getField(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of(timeoutField));
        final String negativeValue = "-10";
        Mockito.when(timeoutField.getValue()).thenReturn(Optional.of(negativeValue));
        Mockito.when(timeoutField.getValues()).thenReturn(List.of(negativeValue));
        Mockito.when(timeoutField.hasValues()).thenReturn(true);

        actionApi.validateConfig(configFieldMap, fieldModel, fieldErrors);
        assertEquals(ERROR_POLARIS_ACCESS_TOKEN, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertEquals(ERROR_POLARIS_TIMEOUT, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT));
    }

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void testConfigWithRealConnectionTestIT() throws Exception {
        final TestProperties testProperties = new TestProperties();

        final String polarisUrl = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_URL);
        final String polarisAccessToken = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_ACCESS_TOKEN);
        final String polarisTimeout = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_TIMEOUT);

        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, polarisUrl);
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, polarisAccessToken);
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_TIMEOUT, polarisTimeout);

        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor);

        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTrustCertificate()).thenReturn(Optional.of(Boolean.TRUE));
        final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        final PolarisProperties polarisProperties = new PolarisProperties(alertProperties, null, proxyManager, new Gson(), new AuthenticationSupport());

        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(polarisProperties);
        try {
            actionApi.testConfig(testConfigModel);
        } catch (final Exception e) {
            e.printStackTrace();
            fail("An exception was thrown while testing (seemingly) valid config. " + e.toString());
        }
    }

    @Test
    public void testConfigWithInvalidFieldsTest() throws Exception {
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null);

        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, "");

        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor);

        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }

        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, "good enough to satisfy the check");

        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }

        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "good enough to satisfy the check");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_TIMEOUT, "");
        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }
    }

    @Test
    public void testConfigThrowsIOExceptionTest() throws IOException, IntegrationException {
        final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = Mockito.mock(AccessTokenPolarisHttpClient.class);
        Mockito.when(accessTokenPolarisHttpClient.attemptAuthentication()).thenThrow(new IntegrationException("Do these exceptions really still happen? Wow!"));

        final PolarisProperties polarisProperties = Mockito.mock(PolarisProperties.class);
        Mockito.when(polarisProperties.createPolarisHttpClient(Mockito.any(IntLogger.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(accessTokenPolarisHttpClient);

        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(polarisProperties);

        final Map<String, ConfigurationFieldModel> keyToValues = new HashMap<>();
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_URL, "good enough to satisfy the check");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_TIMEOUT, "100");
        addConfigurationFieldToMap(keyToValues, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "good enough to satisfy the check");

        final FieldAccessor fieldAccessor = new FieldAccessor(keyToValues);

        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor);

        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected wrapped IOException to be thrown");
        } catch (final IntegrationException e) {
            assertNotNull(e);
        }
    }
}
