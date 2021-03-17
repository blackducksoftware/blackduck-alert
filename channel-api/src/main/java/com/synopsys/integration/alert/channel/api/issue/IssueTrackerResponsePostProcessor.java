/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue;

import com.synopsys.integration.alert.channel.api.issue.model.IssueTrackerResponse;

public interface IssueTrackerResponsePostProcessor {
    void postProcess(IssueTrackerResponse response);

}
