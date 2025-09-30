/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.jira.common.model.components.IssueFieldsComponent;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;

public class JiraCallbackUtilsTest {
    private static final String ISSUE_BASE_URL = "https://www.some-url.com/beforePath";
    private static final String ISSUE_URL = ISSUE_BASE_URL + "/rest/api/afterPath";
    private static final String KEY = "JP-1";
    private static final String EXPECTED_UI_LINK = ISSUE_BASE_URL + "/browse/" + KEY;

    @Test
    void createUILinkReturnsExpected() {
        String output = JiraCallbackUtils.createUILink(ISSUE_URL, KEY);

        assertEquals(EXPECTED_UI_LINK, output);
    }

    @Test
    void createUILinkFromIssueReturnsExpected() {
        IssueFieldsComponent issueFieldsComponent = createIssueFieldsComponent("summary");
        IssueResponseModel responseModel = createIssueResponseModel( "JP-1", KEY, ISSUE_URL, issueFieldsComponent);
        String output = JiraCallbackUtils.createUILink(responseModel);

        assertEquals(EXPECTED_UI_LINK, output);
    }

    @Test
    void createUILinkFromJiraSearcherReturnsExpected() {
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(ISSUE_URL, KEY, null, null);
        String output = JiraCallbackUtils.createUILink(jiraSearcherResponseModel);

        assertEquals(EXPECTED_UI_LINK, output);
    }

    private IssueFieldsComponent createIssueFieldsComponent(String summary) {
        return () -> summary;
    }

    private IssueResponseModel createIssueResponseModel(String id, String key, String self, IssueFieldsComponent issueFieldsComponent) {
        return new IssueResponseModel() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getSelf() {
                return self;
            }

            @Override
            public IssueFieldsComponent getFields() {
                return issueFieldsComponent;
            }
        };
    }
}
