package com.synopsys.integration.alert.provider.polaris.descriptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.polaris.PolarisProviderKey;

public class PolarisDescriptorTest {
    private static final PolarisProviderKey POLARIS_PROVIDER_KEY = new PolarisProviderKey();

    @Disabled
    @Test
    public void createTopicCollectorsTest() {
        // TODO implement
    }

    @Test
    public void getDefinedGlobalFieldsTest() {
        final PolarisGlobalUIConfig polarisGlobalUIConfig = new PolarisGlobalUIConfig();
        final PolarisDescriptor polarisDescriptor = new PolarisDescriptor(POLARIS_PROVIDER_KEY, polarisGlobalUIConfig, null);
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
        final PolarisDescriptor polarisDescriptor = new PolarisDescriptor(POLARIS_PROVIDER_KEY, null, null);
        assertTrue(polarisDescriptor.getAllDefinedFields(null).isEmpty());
    }

}
