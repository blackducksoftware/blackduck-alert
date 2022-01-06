/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.descriptor;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public final class SettingsDescriptorKey extends DescriptorKey {
    private static final String SETTINGS_COMPONENT = "component_settings";

    public SettingsDescriptorKey() {
        super(SETTINGS_COMPONENT, SettingsDescriptor.SETTINGS_LABEL);
    }
}
