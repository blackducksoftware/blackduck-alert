/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.util;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.jira.common.model.response.IssueResponseModel;

public class JiraCallbackUtils {
    private JiraCallbackUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String createUILink(IssueResponseModel issueResponseModel) {
        String userFriendlyUrl = StringUtils.substringBefore(issueResponseModel.getSelf(), "/rest/api");
        return String.format("%s/browse/%s", userFriendlyUrl, issueResponseModel.getKey());
    }

}
