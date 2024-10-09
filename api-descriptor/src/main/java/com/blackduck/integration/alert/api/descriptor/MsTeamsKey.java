/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

@Component
public final class MsTeamsKey extends ChannelKey {
    private static final String COMPONENT_NAME = "msteamskey";
    private static final String MSTEAMS_DISPLAY_NAME = "MS Teams";

    public MsTeamsKey() {
        super(COMPONENT_NAME, MSTEAMS_DISPLAY_NAME);
    }

}
