/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;

@FunctionalInterface
public interface JiraIssueTransitionRetriever {
    TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException;
}
