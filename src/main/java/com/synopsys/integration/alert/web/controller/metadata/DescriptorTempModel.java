package com.synopsys.integration.alert.web.controller.metadata;

import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.util.Stringable;

public class DescriptorTempModel extends Stringable {
    private final String name;
    private final DescriptorType type;
    private final ConfigContextEnum context;
    private final UIComponent uiComponent;

    public DescriptorTempModel(final String name, final DescriptorType type, final ConfigContextEnum context, final UIComponent uiComponent) {
        this.name = name;
        this.type = type;
        this.context = context;
        this.uiComponent = uiComponent;
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public ConfigContextEnum getContext() {
        return context;
    }

    public UIComponent getUiComponent() {
        return uiComponent;
    }
}
