package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
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

    @Test
    public void validateConfigWhenValidTest() {
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null);

        final Map<String, String> fieldErrors = new HashMap<>();
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of("X".repeat(40)));
        Mockito.when(fieldAccessor.getString(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of("100"));

        actionApi.validateConfig(fieldAccessor, fieldErrors);
        assertEquals(null, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertEquals(null, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT));
    }

    @Test
    public void validateConfigWhenInvalidTest() {
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null);

        final Map<String, String> fieldErrors = new HashMap<>();
        final FieldAccessor fieldAccessor = Mockito.mock(FieldAccessor.class);
        Mockito.when(fieldAccessor.getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of("too short"));
        Mockito.when(fieldAccessor.getString(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of("invalid integer"));

        actionApi.validateConfig(fieldAccessor, fieldErrors);
        assertEquals(ERROR_POLARIS_ACCESS_TOKEN, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertEquals(ERROR_POLARIS_TIMEOUT, fieldErrors.get(PolarisDescriptor.KEY_POLARIS_TIMEOUT));

        fieldErrors.clear();
        Mockito.when(fieldAccessor.getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)).thenReturn(Optional.of("too long".repeat(64)));
        Mockito.when(fieldAccessor.getString(PolarisDescriptor.KEY_POLARIS_TIMEOUT)).thenReturn(Optional.of("-10"));

        actionApi.validateConfig(fieldAccessor, fieldErrors);
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

        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(polarisProperties);
        try {
            actionApi.testConfig(testConfigModel);
        } catch (final Exception e) {
            fail("An exception was thrown while testing (seemingly) valid config. " + e.toString());
        }
    }

    @Test
    public void testConfigWithInvalidFieldsTest() {
        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(null);

        final Map<String, FieldValueModel> fieldMap = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fieldMap);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of(), false));
        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of(), false));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of("good enough to satisfy the check"), true));
        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
        }

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_TIMEOUT, new FieldValueModel(Set.of(), false));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of("good enough to satisfy the check"), true));
        try {
            actionApi.testConfig(testConfigModel);
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

        final PolarisGlobalDescriptorActionApi actionApi = new PolarisGlobalDescriptorActionApi(polarisProperties);

        final Map<String, FieldValueModel> fieldMap = new HashMap<>();
        final FieldModel fieldModel = new FieldModel(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL.name(), fieldMap);
        final TestConfigModel testConfigModel = new TestConfigModel(fieldModel);

        fieldMap.put(PolarisDescriptor.KEY_POLARIS_URL, new FieldValueModel(Set.of("good enough to satisfy the check"), true));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_TIMEOUT, new FieldValueModel(Set.of("100"), true));
        fieldMap.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, new FieldValueModel(Set.of("good enough to satisfy the check"), true));

        try {
            actionApi.testConfig(testConfigModel);
            fail("Expected wrapped IOException to be thrown");
        } catch (final IntegrationException e) {
            assertTrue(IOException.class.isInstance(e.getCause()));
        }
    }
}
