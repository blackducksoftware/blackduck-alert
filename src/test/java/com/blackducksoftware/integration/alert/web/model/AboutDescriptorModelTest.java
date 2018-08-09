package com.blackducksoftware.integration.alert.web.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AboutDescriptorModelTest {

    @Test
    public void testGetters() {
        final String iconName = "icon-name";
        final String name = "descriptor-name";
        final AboutDescriptorModel model = new AboutDescriptorModel(iconName, name);
        assertEquals(iconName, model.getIconKey());
        assertEquals(name, model.getName());
    }
}
