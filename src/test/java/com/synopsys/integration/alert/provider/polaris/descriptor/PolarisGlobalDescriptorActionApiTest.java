package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenRestConnection;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class PolarisGlobalDescriptorActionApiTest {
    private static final String ERROR_POLARIS_ACCESS_TOKEN = "Invalid Polaris Access Token.";
    private static final String ERROR_POLARIS_TIMEOUT = "Must be an Integer greater than zero (0).";

    private final PolarisGlobalUIConfig polarisGlobalUIConfig = new PolarisGlobalUIConfig();

    @Test
    public void validateConfigWhenValidTest() {
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter();
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null, modelConverter);

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
    public void validateConfigWhenInvalidTest() {
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter();
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null, modelConverter);

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
        Mockito.when(accessTokenField.getValue()).thenReturn(Optional.of(longTokenString.repeat(64)));
        Mockito.when(accessTokenField.getValues()).thenReturn(List.of(longTokenString.repeat(64)));
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
    public void testConfigWithRealConnectionTestIT() {
        final TestProperties testProperties = new TestProperties();

        final String polarisUrl = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_URL);
        final String polarisAccessToken = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_ACCESS_TOKEN);
        final String polarisTimeout = testProperties.getProperty(TestPropertyKey.TEST_POLARIS_PROVIDER_TIMEOUT);

        final Map<String, FieldValueModel> fieldMap = Map.of(
            PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of(polarisUrl), true),
            PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of(polarisAccessToken), true),
            PolarisDescriptor.KEY_POLARIS_TIMEOUT, new FieldValueModel(Set.of(polarisTimeout), true)
        );
        final FieldModel fieldModel = new FieldModel(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fieldMap);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);

        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        Mockito.when(alertProperties.getAlertTrustCertificate()).thenReturn(Optional.of(Boolean.TRUE));
        final PolarisProperties polarisProperties = new PolarisProperties(alertProperties, null);

        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter();
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(polarisProperties, modelConverter);
        try {
            final Map<String, ConfigField> configFieldMap = polarisGlobalUIConfig.createFields()
                                                                .stream()
                                                                .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
            actionApi.testConfig(configFieldMap, testConfigModel);
        } catch (final Exception e) {
            fail("An exception was thrown while testing (seemingly) valid config. " + e.toString());
        }
    }

    @Test
    public void testConfigWithInvalidFieldsTest() {
        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter();
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null, modelConverter);

        final Map<String, FieldValueModel> fieldMap = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fieldMap);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of(), false));
        final Map<String, ConfigField> configFieldMap = polarisGlobalUIConfig.createFields()
                                                            .stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        try {
            actionApi.testConfig(configFieldMap, testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of(), false));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of("good enough to satisfy the check"), true));
        try {
            actionApi.testConfig(configFieldMap, testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_TIMEOUT, new FieldValueModel(Set.of(), false));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of("good enough to satisfy the check"), true));
        try {
            actionApi.testConfig(configFieldMap, testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }
    }

    @Test
    public void testConfigThrowsIOExceptionTest() throws IOException, IntegrationException {
        final AccessTokenRestConnection accessTokenRestConnection = Mockito.mock(AccessTokenRestConnection.class);
        Mockito.when(accessTokenRestConnection.attemptAuthentication()).thenThrow(new IOException("Do these exceptions really still happen? Wow!"));

        final PolarisProperties polarisProperties = Mockito.mock(PolarisProperties.class);
        Mockito.when(polarisProperties.createRestConnection(Mockito.any(IntLogger.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(accessTokenRestConnection);

        final ConfigurationFieldModelConverter modelConverter = new ConfigurationFieldModelConverter();
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(polarisProperties, modelConverter);

        final Map<String, FieldValueModel> fieldMap = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fieldMap);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of("good enough to satisfy the check"), true));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_TIMEOUT, new FieldValueModel(Set.of("100"), true));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of("good enough to satisfy the check"), true));

        try {
            final Map<String, ConfigField> configFieldMap = polarisGlobalUIConfig.createFields()
                                                                .stream()
                                                                .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
            actionApi.testConfig(configFieldMap, testConfigModel);
            fail("Expected wrapped IOException to be thrown");
        } catch (final IntegrationException e) {
            assertTrue(IOException.class.isInstance(e.getCause()));
        }
    }
}
