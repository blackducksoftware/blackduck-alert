package com.synopsys.integration.alert.web.controller.internal;

import java.util.Collection;

import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.util.Stringable;

public class DescriptorTempModel extends Stringable {
    private final String name;
    private final Collection<UIComponent> uiComponents;

    public DescriptorTempModel(final String name, final Collection<UIComponent> uiComponents) {
        this.name = name;
        this.uiComponents = uiComponents;
    }

    public String getName() {
        return name;
    }

    public Collection<UIComponent> getUIComponents() {
        return uiComponents;
    }
}
