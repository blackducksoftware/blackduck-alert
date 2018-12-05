package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;

public abstract class CommonFieldUIConfig extends UIConfig {

    public abstract UIComponent createUIComponent();

    public abstract List<ConfigField> createCommonConfigFields();

    @Override
    public UIComponent generateUIComponent() {
        final UIComponent uiComponent = createUIComponent();

        final List<ConfigField> commonFields = createCommonConfigFields();
        commonFields.addAll(uiComponent.getFields());

        uiComponent.setFields(commonFields);
        return uiComponent;
    }
}
