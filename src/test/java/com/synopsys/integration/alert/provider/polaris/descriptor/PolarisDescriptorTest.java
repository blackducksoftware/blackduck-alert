package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;

public class PolarisDescriptorTest {
    private final PolarisProvider polarisProvider = new PolarisProvider();

    @Disabled
    @Test
    public void createTopicCollectorsTest() {
        // TODO implement
    }

    @Test
    public void getDefinedGlobalFieldsTest() {
        final PolarisDescriptor polarisDescriptor = new PolarisDescriptor(null, null, null, null, polarisProvider);
        final Collection<DefinedFieldModel> fields = polarisDescriptor.getDefinedFields(ConfigContextEnum.GLOBAL);
        assertNotNull(fields);
        assertContainsFieldWithKey(fields, PolarisDescriptor.KEY_POLARIS_URL);
        assertContainsFieldWithKey(fields, PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN);
        assertContainsFieldWithKey(fields, PolarisDescriptor.KEY_POLARIS_TIMEOUT);
    }

    @Disabled
    @Test
    public void getDefinedDistributionFieldsTest() {

    }

    @Test
    public void getDefinedFieldsForInvalidContextTest() {
        final PolarisDescriptor polarisDescriptor = new PolarisDescriptor(null, null, null, null, polarisProvider);
        assertTrue(polarisDescriptor.getDefinedFields(null).isEmpty());
    }

    private void assertContainsFieldWithKey(final Collection<DefinedFieldModel> fields, final String fieldKey) {
        final boolean containsField = fields
                                          .stream()
                                          .anyMatch(field -> field.getKey().equals(fieldKey));
        assertTrue(containsField, "Did not contain field with key: " + fieldKey);
    }
}
