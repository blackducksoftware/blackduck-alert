package com.synopsys.integration.alert.provider.polaris;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.FormatType;

public class PolarisProviderTest {
    @Disabled
    @Test
    public void initializeTest() {
        // TODO implement
    }

    @Disabled
    @Test
    public void destroy() {
        // TODO implement
    }

    @Disabled
    @Test
    public void getProviderContentTypesTest() {
        // TODO implement
    }

    @Test
    public void getSupportedFormatTypes() {
        final PolarisProvider polarisProvider = new PolarisProvider();
        final Set<FormatType> formatTypes = polarisProvider.getSupportedFormatTypes();

        assertTrue(formatTypes.contains(FormatType.DEFAULT));
        assertTrue(formatTypes.contains(FormatType.DIGEST));
    }
}
