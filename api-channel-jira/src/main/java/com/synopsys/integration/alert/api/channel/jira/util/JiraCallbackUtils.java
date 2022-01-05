/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.util;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;

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
