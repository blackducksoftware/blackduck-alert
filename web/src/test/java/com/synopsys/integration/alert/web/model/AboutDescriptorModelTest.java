package com.synopsys.integration.alert.web.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.web.api.about.AboutDescriptorModel;

public class AboutDescriptorModelTest {

    @Test
    public void testGetters() {
        final String iconName = "icon-name";
        final String name = "descriptor-name";
        AboutDescriptorModel model = new AboutDescriptorModel(iconName, name);
        assertEquals(iconName, model.getIconKey());
        assertEquals(name, model.getName());
    }
}
