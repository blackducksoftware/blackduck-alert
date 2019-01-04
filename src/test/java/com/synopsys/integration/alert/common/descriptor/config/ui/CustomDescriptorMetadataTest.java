package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CustomDescriptorMetadataTest {

    @Test
    public void getUiComponentNameTest() {
        final String name = "test name";
        final CustomDescriptorMetadata customDescriptorMetadata = new CustomDescriptorMetadata(null, null, null, null, null, null, name);

        final String storedName = customDescriptorMetadata.getUiComponentName();
        assertEquals(name, storedName);
    }

    @Test
    public void setUiComponentNameTest() {
        final String name = "test name";
        final CustomDescriptorMetadata customDescriptorMetadata = new CustomDescriptorMetadata(null, null, null, null, null, null, name);

        final String newName = "new name";
        customDescriptorMetadata.setUiComponentName(newName);
        assertEquals(newName, customDescriptorMetadata.getUiComponentName());
    }
}
