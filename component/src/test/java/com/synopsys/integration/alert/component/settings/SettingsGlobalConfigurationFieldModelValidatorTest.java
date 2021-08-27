package com.synopsys.integration.alert.component.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.validator.SettingsGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

public class SettingsGlobalConfigurationFieldModelValidatorTest {

    /*
     * encryption password: required, minimum encryption length
     * encryption salt: required, minimum encryption length
     *
     * proxy host: needs proxy port
     * proxy port: needs proxy host, is a number
     *
     * proxy username: needs proxy host, needs proxy password
     * proxy password: needs proxy host, needs proxy username
     */

    @Test
    public void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertValid();
    }

    @Test
    public void saltTooShort() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertInvalidValue(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, "short");
    }

    @Test
    public void passwordTooShort() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertInvalidValue(SettingsDescriptor.KEY_ENCRYPTION_PWD, "short");
    }

    @Test
    public void validHostAndPort() {
        Map<String, FieldValueModel> keyToValues = createProxyKeyToValues();

        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(keyToValues);
        validatorAsserter.assertValid();
    }

    @Test
    public void missingProxyPassword() {
        Map<String, FieldValueModel> keyToValues = createProxyKeyToValues();
        FieldValueModel username = new FieldValueModel(List.of("username"), true);
        keyToValues.put(ProxyManager.KEY_PROXY_USERNAME, username);

        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter(keyToValues);
        validatorAsserter.assertMissingValue(ProxyManager.KEY_PROXY_PWD);
    }

    private GlobalConfigurationValidatorAsserter createValidatorAsserter() {
        return createValidatorAsserter(createKeyToValues());
    }

    private GlobalConfigurationValidatorAsserter createValidatorAsserter(Map<String, FieldValueModel> keyToValues) {
        return new GlobalConfigurationValidatorAsserter(new SettingsDescriptorKey().getUniversalKey(), new SettingsGlobalConfigurationFieldModelValidator(), keyToValues);
    }

    private Map<String, FieldValueModel> createKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();

        FieldValueModel encryptionPassword = new FieldValueModel(List.of("encryptionPassword"), true);
        FieldValueModel encryptionSalt = new FieldValueModel(List.of("encryptionSalt"), true);

        keyToValues.put(SettingsDescriptor.KEY_ENCRYPTION_PWD, encryptionPassword);
        keyToValues.put(SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT, encryptionSalt);

        return keyToValues;
    }

    private Map<String, FieldValueModel> createProxyKeyToValues() {
        Map<String, FieldValueModel> keyToValues = createKeyToValues();
        FieldValueModel proxyHost = new FieldValueModel(List.of("proxyHost"), true);
        FieldValueModel proxyPort = new FieldValueModel(List.of("99"), true);

        keyToValues.put(ProxyManager.KEY_PROXY_HOST, proxyHost);
        keyToValues.put(ProxyManager.KEY_PROXY_PORT, proxyPort);

        return keyToValues;
    }
}
