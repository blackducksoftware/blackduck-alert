/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import java.io.Serializable;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;

public interface ProjectIssueFinder<T extends Serializable> {
    IssueTrackerSearchResult<T> findProjectIssues(ProviderDetails providerDetails, LinkableItem project) throws AlertException;

}
