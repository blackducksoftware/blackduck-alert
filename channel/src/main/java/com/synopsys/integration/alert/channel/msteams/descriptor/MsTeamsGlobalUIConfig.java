/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class MsTeamsGlobalUIConfig extends UIConfig {
    public MsTeamsGlobalUIConfig() {
        super(MsTeamsDescriptor.MSTEAMS_LABEL, MsTeamsDescriptor.MSTEAMS_DESCRIPTION, MsTeamsDescriptor.MSTEAMS_URL);
    }

    @Override
    public List<ConfigField> createFields() {
        return List.of();
    }

}
