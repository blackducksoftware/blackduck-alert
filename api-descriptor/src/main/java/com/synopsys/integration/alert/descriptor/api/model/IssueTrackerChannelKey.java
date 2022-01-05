/*
 * api-descriptor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.descriptor.api.model;

public abstract class IssueTrackerChannelKey extends ChannelKey {
    public IssueTrackerChannelKey(String universalKey, String displayName) {
        super(universalKey, displayName);
    }
}
