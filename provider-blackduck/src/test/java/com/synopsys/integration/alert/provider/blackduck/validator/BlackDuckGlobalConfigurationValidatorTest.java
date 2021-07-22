package com.synopsys.integration.alert.provider.blackduck.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

public class BlackDuckGlobalConfigurationValidatorTest {

    /*
     * Provider config name: Required, no duplicate names
     * Url: required, valid Url
     * API key: required, Specific length
     * Timeout: required, long timeout warning, < 0 timeout error
     */

    @Test
    public void verifyValidConfiguration() {
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldModel fieldModel = new FieldModel(new BlackDuckProviderKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);

        BlackDuckGlobalConfigurationValidator blackDuckGlobalConfigurationValidator = new BlackDuckGlobalConfigurationValidator(createDefaultConfigurationAccessor());
        Set<AlertFieldStatus> alertFieldStatuses = blackDuckGlobalConfigurationValidator.validate(fieldModel);

        assertEquals(0, alertFieldStatuses.size());
    }

    @Test
    public void nonUniqueName() {
        String duplicateName = "duplicateName";
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldValueModel apiKeyFieldValueModel = new FieldValueModel(List.of(duplicateName), true);
        defaultKeyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, apiKeyFieldValueModel);
        FieldModel fieldModel = new FieldModel(new BlackDuckProviderKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);

        ConfigurationFieldModel configurationFieldModel = Mockito.mock(ConfigurationFieldModel.class);
        Mockito.when(configurationFieldModel.getFieldValue()).thenReturn(Optional.of(duplicateName));

        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModel.getDescriptorContext()).thenReturn(ConfigContextEnum.GLOBAL);
        Mockito.when(configurationModel.getField(Mockito.any())).thenReturn(Optional.of(configurationFieldModel));

        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorType(Mockito.any())).thenReturn(List.of(configurationModel));
        BlackDuckGlobalConfigurationValidator blackDuckGlobalConfigurationValidator = new BlackDuckGlobalConfigurationValidator(configurationAccessor);
        Set<AlertFieldStatus> alertFieldStatuses = blackDuckGlobalConfigurationValidator.validate(fieldModel);

        assertEquals(1, alertFieldStatuses.size());

        AlertFieldStatus alertFieldStatus = alertFieldStatuses.stream().findFirst().orElse(null);
        assertNotNull(alertFieldStatus);
        assertEquals(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, alertFieldStatus.getFieldName());
    }

    @Test
    public void invalidUrl() {
        assertInvalidValue(BlackDuckDescriptor.KEY_BLACKDUCK_URL, "badUrl");
    }

    @Test
    public void givesTimeoutWarning() {
        assertInvalidValue(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, "500", (fieldStatus) -> {
            assertEquals(FieldStatusSeverity.WARNING, fieldStatus.getSeverity());
        });
    }

    @Test
    public void givesTimeoutError() {
        assertInvalidValue(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, "0", (fieldStatus) -> {
            assertEquals(FieldStatusSeverity.ERROR, fieldStatus.getSeverity());
        });
    }

    @Test
    public void apiKeyTooShort() {
        assertInvalidValue(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, "too short");
    }

    private void assertInvalidValue(String key, String invalidValue) {
        assertInvalidValue(key, invalidValue, (value) -> {});
    }

    private void assertInvalidValue(String key, String invalidValue, Consumer<AlertFieldStatus> additionalAsserts) {
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldValueModel apiKeyFieldValueModel = new FieldValueModel(List.of(invalidValue), true);
        defaultKeyToValues.put(key, apiKeyFieldValueModel);
        FieldModel fieldModel = new FieldModel(new BlackDuckProviderKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);

        BlackDuckGlobalConfigurationValidator blackDuckGlobalConfigurationValidator = new BlackDuckGlobalConfigurationValidator(createDefaultConfigurationAccessor());
        Set<AlertFieldStatus> alertFieldStatuses = blackDuckGlobalConfigurationValidator.validate(fieldModel);

        assertEquals(1, alertFieldStatuses.size());

        AlertFieldStatus alertFieldStatus = alertFieldStatuses.stream().findFirst().orElse(null);
        assertNotNull(alertFieldStatus);
        assertEquals(key, alertFieldStatus.getFieldName());
        additionalAsserts.accept(alertFieldStatus);
    }

    private ConfigurationAccessor createDefaultConfigurationAccessor() {
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getConfigurationsByDescriptorType(Mockito.any())).thenReturn(List.of());
        return configurationAccessor;
    }

    private Map<String, FieldValueModel> createDefaultKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        FieldValueModel nameFieldValueModel = new FieldValueModel(List.of("uniqueName"), true);
        FieldValueModel urlFieldValueModel = new FieldValueModel(List.of("https://google.com"), true);
        FieldValueModel apiKeyFieldValueModel = new FieldValueModel(List.of("This should be long enough to pass: aksjdfalkfalksfsdfljahfjdasjkdfhlajfhlkasjhdflaskjhdflkasjhflksajhflaksjhflkasjhdflakjshfldjakfasjf"), true);
        FieldValueModel timeoutFieldValueModel = new FieldValueModel(List.of("300"), true);

        keyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME, nameFieldValueModel);
        keyToValues.put(BlackDuckDescriptor.KEY_BLACKDUCK_URL, urlFieldValueModel);
        keyToValues.put(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, apiKeyFieldValueModel);
        keyToValues.put(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, timeoutFieldValueModel);

        return keyToValues;
    }
}
