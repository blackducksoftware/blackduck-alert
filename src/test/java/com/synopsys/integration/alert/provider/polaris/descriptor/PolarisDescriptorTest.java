package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;

public class PolarisDescriptorTest {
    private final PolarisProvider polarisProvider = new PolarisProvider(null, null, null);

    @Disabled
    @Test
    public void createTopicCollectorsTest() {
        // TODO implement
    }

    @Test
    public void getDefinedGlobalFieldsTest() {
        final PolarisGlobalUIConfig polarisGlobalUIConfig = new PolarisGlobalUIConfig();
        final PolarisDescriptor polarisDescriptor = new PolarisDescriptor(null, polarisGlobalUIConfig, null, null, polarisProvider);
        final Set<String> fields = polarisDescriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL).stream().map(DefinedFieldModel::getKey).collect(Collectors.toSet());
        assertNotNull(fields);
        assertTrue(fields.contains(PolarisDescriptor.KEY_POLARIS_URL));
        assertTrue(fields.contains(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN));
        assertTrue(fields.contains(PolarisDescriptor.KEY_POLARIS_TIMEOUT));
    }

    @Disabled
    @Test
    public void getDefinedDistributionFieldsTest() {

    }

    @Test
    public void getDefinedFieldsForInvalidContextTest() {
        final PolarisDescriptor polarisDescriptor = new PolarisDescriptor(null, null, null, null, polarisProvider);
        assertTrue(polarisDescriptor.getAllDefinedFields(null).isEmpty());
    }
}
