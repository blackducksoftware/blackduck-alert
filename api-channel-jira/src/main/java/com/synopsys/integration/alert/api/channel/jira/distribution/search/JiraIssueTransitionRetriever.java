/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

@FunctionalInterface
public interface JiraIssueTransitionRetriever {
    TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException;
}
