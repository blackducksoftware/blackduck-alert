/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.search;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;

public interface ExactIssueFinder<T extends Serializable> {
    List<ExistingIssueDetails<T>> findExistingIssuesByProjectIssueModel(ProjectIssueModel projectIssueModel) throws AlertException;

}
