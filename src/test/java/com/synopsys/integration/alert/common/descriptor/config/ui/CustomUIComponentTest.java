package com.synopsys.integration.alert.common.descriptor.config.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CustomUIComponentTest {

    @Test
    public void getUiComponentNameTest() {
        final String name = "test name";
        final CustomUIComponent customUIComponent = new CustomUIComponent(null, null, null, null, name);

        final String storedName = customUIComponent.getUiComponentName();
        assertEquals(name, storedName);
    }

    @Test
    public void setUiComponentNameTest() {
        final String name = "test name";
        final CustomUIComponent customUIComponent = new CustomUIComponent(null, null, null, null, name);

        final String newName = "new name";
        customUIComponent.setUiComponentName(newName);
        assertEquals(newName, customUIComponent.getUiComponentName());
    }
}
