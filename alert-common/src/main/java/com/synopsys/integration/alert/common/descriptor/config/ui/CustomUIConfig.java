/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;

/**
 * By extending this class you are going to use a component predefined in the javascript UI code rather than field generation.
 * The javascript components need to be located in the js/component/dynamic/loaded directory.
 * The javascript component that exists for this CustomUIConfig class needs to be registered in the DescriptorContentLoader.js file.
 */
public abstract class CustomUIConfig extends UIConfig {

    public CustomUIConfig(String label, String description, String urlName, String componentPath) {
        super(label, description, urlName, componentPath);
    }

    @Override
    public List<ConfigField> createFields() {
        return List.of();
    }

}
