/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

class ProxyConfigurationModelConverterTest {
    public static final List<String> TEST_NON_PROXY_HOSTS = List.of("host-1.example.com", "host-2.example.com", "host-3.example.com");
    public static final String TEST_SMTP_HOST = "proxy.server.example.com";
    public static final String TEST_AUTH_PASSWORD = "apassword";
    public static final String TEST_SMTP_PORT = "3025";
    public static final String TEST_AUTH_USER = "auser";

    private final SettingsProxyValidator validator = new SettingsProxyValidator();

    @Test
    void validConversionTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        ProxyConfigurationModelConverter converter = new ProxyConfigurationModelConverter(validator);
        Optional<SettingsProxyModel> model = converter.convertAndValidate(configurationModel, null);
        assertTrue(model.isPresent());
        SettingsProxyModel proxyModel = model.get();
        assertEquals(TEST_AUTH_USER, proxyModel.getProxyUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, proxyModel.getProxyPassword().orElse(null));
        assertEquals(TEST_SMTP_HOST, proxyModel.getProxyHost());
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), proxyModel.getProxyPort());
        assertEquals(TEST_NON_PROXY_HOSTS, proxyModel.getNonProxyHosts().orElse(null));

    }

    @Test
    void validConversionExistingConfigIgnoredTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        ProxyConfigurationModelConverter converter = new ProxyConfigurationModelConverter(validator);
        // Note: Since only one proxy configuration is present, the existing ID field is just ignored.
        Optional<SettingsProxyModel> model = converter.convertAndValidate(configurationModel, UUID.randomUUID().toString());
        assertTrue(model.isPresent());
        SettingsProxyModel proxyModel = model.get();
        assertEquals(TEST_AUTH_USER, proxyModel.getProxyUsername().orElse(null));
        assertEquals(TEST_AUTH_PASSWORD, proxyModel.getProxyPassword().orElse(null));
        assertEquals(TEST_SMTP_HOST, proxyModel.getProxyHost());
        assertEquals(Integer.valueOf(TEST_SMTP_PORT), proxyModel.getProxyPort());
        assertEquals(TEST_NON_PROXY_HOSTS, proxyModel.getNonProxyHosts().orElse(null));

    }

    @Test
    void invalidPortTest() {
        ConfigurationModel configurationModel = createDefaultConfigurationModel();
        configurationModel.getField(ProxyConfigurationModelConverter.FIELD_KEY_PORT)
            .ifPresent(field -> field.setFieldValue("twenty-five"));
        ProxyConfigurationModelConverter converter = new ProxyConfigurationModelConverter(validator);
        Optional<SettingsProxyModel> model = converter.convertAndValidate(configurationModel, null);
        assertTrue(model.isEmpty());
    }

    @Test
    void emptyFieldsTest() {
        ConfigurationModel emptyModel = new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, Map.of());
        ProxyConfigurationModelConverter converter = new ProxyConfigurationModelConverter(validator);
        Optional<SettingsProxyModel> model = converter.convertAndValidate(emptyModel, null);
        assertTrue(model.isEmpty());
    }

    private ConfigurationModel createDefaultConfigurationModel() {
        Map<String, ConfigurationFieldModel> fieldValuesMap = new HashMap<>();

        ConfigurationFieldModel hostField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_HOST);
        ConfigurationFieldModel portField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_PORT);
        ConfigurationFieldModel passwordField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_PASSWORD);
        ConfigurationFieldModel userField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_USERNAME);
        ConfigurationFieldModel nonProxyHostField = ConfigurationFieldModel.create(ProxyConfigurationModelConverter.FIELD_KEY_NON_PROXY_HOSTS);

        hostField.setFieldValue(TEST_SMTP_HOST);
        portField.setFieldValue(TEST_SMTP_PORT);
        passwordField.setFieldValue(TEST_AUTH_PASSWORD);
        userField.setFieldValue(TEST_AUTH_USER);
        nonProxyHostField.setFieldValues(TEST_NON_PROXY_HOSTS);

        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_HOST, hostField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_PORT, portField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_PASSWORD, passwordField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_USERNAME, userField);
        fieldValuesMap.put(ProxyConfigurationModelConverter.FIELD_KEY_NON_PROXY_HOSTS, nonProxyHostField);
        return new ConfigurationModel(1L, 1L, "", "", ConfigContextEnum.GLOBAL, fieldValuesMap);
    }

}
