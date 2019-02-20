package com.synopsys.integration.alert.provider.polaris;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.data.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.data.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.data.model.ConfigurationModel;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.provider.polaris.descriptor.PolarisDescriptor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class PolarisPropertiesTest {
    private static final String POLARIS_URL = "https://polaris";
    private static final Integer POLARIS_TIMEOUT = 100;
    private static final String POLARIS_ACCESS_TOKEN = "access_token";

    private final BaseConfigurationAccessor configurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
    private final ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
    private final ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
    private final Gson gson = new Gson();
    private final AuthenticationSupport authenticationSupport = new AuthenticationSupport();

    @Test
    public void getUrlTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_URL);
        field.setFieldValue(POLARIS_URL);

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(PolarisDescriptor.KEY_POLARIS_URL, field)
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor, proxyManager, gson, authenticationSupport);
        assertEquals(POLARIS_URL, polarisProperties.getUrl().orElse(null));
    }

    @Test
    public void getUrlWhenEmptyTest() throws AlertDatabaseConstraintException {
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of()
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor, proxyManager, gson, authenticationSupport);
        assertEquals(Optional.empty(), polarisProperties.getUrl());
    }

    @Test
    public void getTimeoutTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel field = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_TIMEOUT);
        field.setFieldValue(POLARIS_TIMEOUT.toString());

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(PolarisDescriptor.KEY_POLARIS_TIMEOUT, field)
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor, proxyManager, gson, authenticationSupport);
        assertEquals(POLARIS_TIMEOUT, polarisProperties.getTimeout());
    }

    @Test
    public void getTimeoutDefaultTest() throws AlertDatabaseConstraintException {
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of()
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor, proxyManager, gson, authenticationSupport);
        assertEquals(PolarisProperties.DEFAULT_TIMEOUT, polarisProperties.getTimeout());
    }

    @Test
    public void createRestConnectionTest() throws IntegrationException {
        final ConfigurationFieldModel urlField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_URL);
        urlField.setFieldValue(POLARIS_URL);
        final ConfigurationFieldModel timeoutField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_TIMEOUT);
        timeoutField.setFieldValue(POLARIS_TIMEOUT.toString());
        final ConfigurationFieldModel accessTokenField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN);
        accessTokenField.setFieldValue(POLARIS_ACCESS_TOKEN);

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(
                PolarisDescriptor.KEY_POLARIS_URL, urlField,
                PolarisDescriptor.KEY_POLARIS_TIMEOUT, timeoutField,
                PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, accessTokenField
            )
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final AlertProperties mockAlertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);

        final Logger logger = LoggerFactory.getLogger(getClass());
        final PolarisProperties polarisProperties = new PolarisProperties(mockAlertProperties, configurationAccessor, proxyManager, gson, authenticationSupport);
        try {
            polarisProperties.createPolarisHttpClient(logger);
        } catch (final IntegrationException e) {
            fail("Did not expect an exception to be thrown");
        }
    }

    @Test
    public void createRestConnectionThrowsExceptionsTest() throws AlertDatabaseConstraintException {
        final IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final AlertProperties mockAlertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);

        final ConfigurationFieldModel urlField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_URL);
        urlField.setFieldValue(POLARIS_URL);
        final ConfigurationFieldModel timeoutField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_TIMEOUT);
        timeoutField.setFieldValue(POLARIS_TIMEOUT.toString());
        final ConfigurationFieldModel accessTokenField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN);
        accessTokenField.setFieldValue(POLARIS_ACCESS_TOKEN);

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(
                PolarisDescriptor.KEY_POLARIS_TIMEOUT, timeoutField,
                PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, accessTokenField
            )
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties1 = new PolarisProperties(mockAlertProperties, configurationAccessor, proxyManager, gson, authenticationSupport);
        try {
            polarisProperties1.createPolarisHttpClient(intLogger);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException urlException) {
            assertEquals("The field baseUrl cannot be blank", urlException.getMessage());
        }

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(
                PolarisDescriptor.KEY_POLARIS_URL, urlField,
                PolarisDescriptor.KEY_POLARIS_TIMEOUT, timeoutField
            )
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));
        final PolarisProperties polarisProperties2 = new PolarisProperties(mockAlertProperties, configurationAccessor, proxyManager, gson, authenticationSupport);
        try {
            polarisProperties2.createPolarisHttpClient(intLogger);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException accessTokenException) {
            assertEquals("The field accessToken cannot be blank", accessTokenException.getMessage());
        }
    }

    @Test
    public void createRestConnectionSafelyTest() throws AlertDatabaseConstraintException {
        final ConfigurationFieldModel urlField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_URL);
        urlField.setFieldValue(POLARIS_URL);
        final ConfigurationFieldModel timeoutField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_TIMEOUT);
        timeoutField.setFieldValue(POLARIS_TIMEOUT.toString());
        final ConfigurationFieldModel accessTokenField = ConfigurationFieldModel.create(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN);
        accessTokenField.setFieldValue(POLARIS_ACCESS_TOKEN);

        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of(
                PolarisDescriptor.KEY_POLARIS_URL, urlField,
                PolarisDescriptor.KEY_POLARIS_TIMEOUT, timeoutField,
                PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, accessTokenField
            )
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final AlertProperties mockAlertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);

        final Logger logger = LoggerFactory.getLogger(getClass());
        final PolarisProperties polarisProperties = new PolarisProperties(mockAlertProperties, configurationAccessor, proxyManager, gson, authenticationSupport);
        final Optional<AccessTokenPolarisHttpClient> accessTokenPolarisHttpClient = polarisProperties.createPolarisHttpClientSafely(logger);
        assertTrue(accessTokenPolarisHttpClient.isPresent(), "Expected optional RestConnection to be present");
    }

    @Test
    public void createRestConnectionSafelyReturnsEmptyTest() throws AlertDatabaseConstraintException {
        Mockito.when(configurationModel.getCopyOfKeyToFieldMap()).thenReturn(
            Map.of()
        );
        Mockito.when(configurationAccessor.getConfigurationByDescriptorNameAndContext(PolarisProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL)).thenReturn(List.of(configurationModel));

        final PolarisProperties polarisProperties = new PolarisProperties(null, configurationAccessor, proxyManager, gson, authenticationSupport);
        assertEquals(Optional.empty(), polarisProperties.createPolarisHttpClientSafely((IntLogger) null));
    }
}
