package com.synopsys.integration.alert.web.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.web.api.about.AboutDescriptorModel;

public class AboutDescriptorModelTest {

    @Test
    public void testGetters() {
        final String iconName = "icon-name";
        final String name = "descriptor-name";
        AboutDescriptorModel model = new AboutDescriptorModel(iconName, name);
        Assert.assertEquals(iconName, model.getIconKey());
        Assert.assertEquals(name, model.getName());
    }
}
