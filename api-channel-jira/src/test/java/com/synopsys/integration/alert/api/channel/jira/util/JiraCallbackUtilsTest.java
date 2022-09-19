package com.synopsys.integration.alert.api.channel.jira.util;

import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JiraCallbackUtilsTest {
    static final String ISSUE_BASE_URL = "https://www.some-url.com/beforePath";
    static final String ISSUE_URL = ISSUE_BASE_URL + "/rest/api/afterPath";
    static final String KEY = "JP-1";
    static final String EXPECTED_UI_LINK = ISSUE_BASE_URL + "/browse/" + KEY;

    @Test
    void createUILinkReturnsCorrectLink() {
        String output = JiraCallbackUtils.createUILink(ISSUE_URL, KEY);

        assertEquals(EXPECTED_UI_LINK, output);
    }

    @Test
    void createUILinkFromIssueResponseModelReturnsCorrectLink() {
        IssueFieldsComponent issueFieldsComponent = new IssueFieldsComponent(List.of(), null, null, "summary", "description", List.of(), null, List.of(), null, null, null, null);
        IssueResponseModel responseModel = new IssueResponseModel("", "JP-1", ISSUE_URL, KEY, Map.of(), Map.of(), Map.of(), Map.of(), List.of(), null, null, null, null, null, issueFieldsComponent);
        String output = JiraCallbackUtils.createUILink(responseModel);

        assertEquals(EXPECTED_UI_LINK, output);
    }

    @Test
    void createUILinkFromJiraSearcherResponseModelReturnsCorrectLink() {
        JiraSearcherResponseModel jiraSearcherResponseModel = new JiraSearcherResponseModel(ISSUE_URL, KEY, null, null);
        String output = JiraCallbackUtils.createUILink(jiraSearcherResponseModel);

        assertEquals(EXPECTED_UI_LINK, output);
    }
}
