package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;

public class PolarisGlobalUIConfigTest {
    @Test
    public void createFieldsTest() {
        EncryptionSettingsValidator encryptionValidator = Mockito.mock(EncryptionSettingsValidator.class);
        Mockito.when(encryptionValidator.apply(Mockito.any(), Mockito.any())).thenReturn(List.of());
        final PolarisGlobalUIConfig uiConfig = new PolarisGlobalUIConfig(encryptionValidator);
        final List<ConfigField> fields = uiConfig.createFields();
        assertNotNull(fields);
        assertContainsFieldWithKey(fields, PolarisDescriptor.KEY_POLARIS_URL);
        assertContainsFieldWithKey(fields, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN);
        assertContainsFieldWithKey(fields, PolarisDescriptor.KEY_POLARIS_TIMEOUT);
    }

    private void assertContainsFieldWithKey(final List<ConfigField> fields, final String fieldKey) {
        final boolean containsField = fields
                                          .stream()
                                          .anyMatch(field -> field.getKey().equals(fieldKey));
        assertTrue(containsField, "Did not contain field with key: " + fieldKey);
    }
}
