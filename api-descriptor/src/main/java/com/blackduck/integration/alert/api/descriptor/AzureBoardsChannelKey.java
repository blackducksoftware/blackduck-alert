/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.IssueTrackerChannelKey;

@Component
public class AzureBoardsChannelKey extends IssueTrackerChannelKey {
    private static final String COMPONENT_NAME = "channel_azure_boards";
    private static final String AZURE_DISPLAY_NAME = "Azure Boards";

    public AzureBoardsChannelKey() {
        super(COMPONENT_NAME, AZURE_DISPLAY_NAME);
    }

}
