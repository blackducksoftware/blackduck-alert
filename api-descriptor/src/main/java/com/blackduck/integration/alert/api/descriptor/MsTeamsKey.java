/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import org.springframework.stereotype.Component;

@Component
public final class MsTeamsKey extends ChannelKey {
    private static final String COMPONENT_NAME = "msteamskey";
    private static final String MSTEAMS_DISPLAY_NAME = "MS Teams";

    public MsTeamsKey() {
        super(COMPONENT_NAME, MSTEAMS_DISPLAY_NAME);
    }

}
