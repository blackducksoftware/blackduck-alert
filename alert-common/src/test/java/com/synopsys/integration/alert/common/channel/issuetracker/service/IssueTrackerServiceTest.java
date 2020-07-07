package com.synopsys.integration.alert.common.channel.issuetracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueCommentRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueCreationRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.channel.issuetracker.service.util.TestContext;
import com.synopsys.integration.alert.common.channel.issuetracker.service.util.TestServerConfig;
import com.synopsys.integration.exception.IntegrationException;

public class IssueTrackerServiceTest {
    private static final String EXPECTED_STATUS = "Good Status";

    @Test
    public void testIssueTrackerService() throws Exception {
        Gson gson = new Gson();
        TestServerConfig serverConfig = new TestServerConfig();
        IssueConfig issueConfig = new IssueConfig();
        TestContext context = new TestContext(serverConfig, issueConfig);

        IssueTrackerService<TestContext> service = new IssueTrackerService<TestContext>(gson) {
            @Override
            public IssueTrackerResponse sendRequests(TestContext context, List<IssueTrackerRequest> requests) throws IntegrationException {
                return new IssueTrackerResponse(EXPECTED_STATUS, new ArrayList<>());
            }
        };

        List<IssueTrackerRequest> requests = new ArrayList<>();
        requests.add(IssueCreationRequest.of(null, null));
        requests.add(IssueCommentRequest.of(null, null));
        requests.add(IssueResolutionRequest.of(null, null));

        IssueTrackerResponse response = service.sendRequests(context, requests);

        assertEquals(serverConfig, context.getIssueTrackerConfig());
        assertEquals(issueConfig, context.getIssueConfig());
        assertEquals(gson, service.getGson());
        assertEquals(EXPECTED_STATUS, response.getStatusMessage());
        assertTrue(response.getUpdatedIssueKeys().isEmpty());
    }
}
