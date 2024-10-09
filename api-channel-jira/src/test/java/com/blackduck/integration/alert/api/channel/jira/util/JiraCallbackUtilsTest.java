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
        IssueFieldsComponent issueFieldsComponent = new IssueFieldsComponent(List.of(), null, null, "summary", "description", List.of(), null, List.of(), null, null, null, null);
        IssueResponseModel responseModel = new IssueResponseModel("", "JP-1", ISSUE_URL, KEY, Map.of(), Map.of(), Map.of(), Map.of(), List.of(), null, null, null, null, null, issueFieldsComponent);
        String output = JiraCallbackUtils.createUILink(responseModel);

        assertEquals(EXPECTED_UI_LINK, output);
    }

    @Test
    void createUILinkFromJiraSearcherReturnsExpected() {
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(ISSUE_URL, KEY, null, null);
        String output = JiraCallbackUtils.createUILink(jiraSearcherResponseModel);

        assertEquals(EXPECTED_UI_LINK, output);
    }
}
