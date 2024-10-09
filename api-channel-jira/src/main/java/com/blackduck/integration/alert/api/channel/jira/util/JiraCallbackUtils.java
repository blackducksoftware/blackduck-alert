/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.util;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;

public class JiraCallbackUtils {
    private JiraCallbackUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String createUILink(IssueResponseModel issueResponseModel) {
        return createUILink(issueResponseModel.getSelf(), issueResponseModel.getKey());
    }

    public static String createUILink(JiraSearcherResponseModel issueResponseModel) {
        return createUILink(issueResponseModel.getIssueUrl(), issueResponseModel.getIssueKey());
    }

    public static String createUILink(String issueUrl, String issueKey) {
        String userFriendlyUrl = StringUtils.substringBefore(issueUrl, "/rest/api");
        return String.format("%s/browse/%s", userFriendlyUrl, issueKey);
    }

}
