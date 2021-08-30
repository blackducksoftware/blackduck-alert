package com.synopsys.integration.alert.api.channel.issue.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

class IssueTrackerSimpleMessageConverterTest {
    private static final SimpleMessage SIMPLE_MESSAGE = SimpleMessage.original(
        new ProviderDetails(100L, new LinkableItem("Provider", "A provider", "https://provider")),
        "Example simple message summary",
        "This is the description; it is typically longer than the summary. Something, something, something DARK SIDE...something, something, something COMPLETE!",
        List.of(
            new LinkableItem("Linked Detail", "Here's a detail with a link", "https://link"),
            new LinkableItem("Detail Label", "This one doesn't have a link")
        )
    );

    @Test
    void convertToIssueCreationModelTruncateTitleTest() {
        int maxTitleLength = 10;
        int maxDescriptionLength = 12;
        MockIssueTrackerMessageFormatter formatter = new MockIssueTrackerMessageFormatter(maxTitleLength, maxDescriptionLength, 1000);
        IssueTrackerSimpleMessageConverter converter = new IssueTrackerSimpleMessageConverter(formatter);

        IssueCreationModel issueCreationModel = converter.convertToIssueCreationModel(SIMPLE_MESSAGE, "jobName");
        assertEquals(maxTitleLength, issueCreationModel.getTitle().length());
        assertTrue(maxDescriptionLength >= issueCreationModel.getDescription().length(), "Expected max description length to be greater than or equal to the created description");
        assertTrue(issueCreationModel.getPostCreateComments().size() > 0, "Expected truncated title/description to continue in comments");
    }

    @Test
    void convertToIssueCreationModelUnboundedTest() {
        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        IssueTrackerSimpleMessageConverter converter = new IssueTrackerSimpleMessageConverter(formatter);

        IssueCreationModel issueCreationModel = converter.convertToIssueCreationModel(SIMPLE_MESSAGE, "jobName");
        assertTrue(issueCreationModel.getTitle().contains(SIMPLE_MESSAGE.getSummary()), "Expected title to contain the simple message's summary");

        String issueCreationModelDescription = issueCreationModel.getDescription();
        assertTrue(issueCreationModelDescription.contains(SIMPLE_MESSAGE.getDescription()), "Expected description to contain the simple message's description");
        for (LinkableItem detail : SIMPLE_MESSAGE.getDetails()) {
            assertTrue(issueCreationModelDescription.contains(detail.getLabel()) && issueCreationModelDescription.contains(detail.getValue()), "Expected description to contain the simple message's details");
        }
    }

}
