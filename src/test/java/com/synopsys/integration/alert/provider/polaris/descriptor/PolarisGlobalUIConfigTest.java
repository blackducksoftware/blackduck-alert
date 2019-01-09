package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;

public class PolarisGlobalUIConfigTest {
    @Test
    public void createFieldsTest() {
        final PolarisGlobalUIConfig uiConfig = new PolarisGlobalUIConfig();
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
