package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerContentLengthException;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

class IssueTrackerMessageParserTest {

    @Test
    void createIssueContentModelTest() throws AlertException {
        // Testing with strict limit was breaking the IssueContentModel generation.
        int titleSizeLimit = 5;
        int messageSizeLimit = 10;

        IssueContentModel issueContentModel = createIssueContentModel(titleSizeLimit, messageSizeLimit);
        validateModel(issueContentModel, titleSizeLimit, messageSizeLimit);
    }

    @Test
    void createIssueContentModelDescriptionSplitTest() throws AlertException {
        int titleSizeLimit = 20;
        int messageSizeLimit = 100;

        IssueContentModel issueContentModel = createIssueContentModel(titleSizeLimit, messageSizeLimit);
        Assertions.assertTrue(issueContentModel.getDescription().length() <= messageSizeLimit, "The initial description should be longer than the limit, thus the issue description should be truncated to a length <= the limit.");
        Assertions.assertEquals(3, issueContentModel.getDescriptionComments().size(), "The number of the description components should be 3. First being the truncated description, the rest being additional description items.");
        validateModel(issueContentModel, titleSizeLimit, messageSizeLimit);
    }

    private void validateModel(IssueContentModel issueContentModel, int titleSizeLimit, int messageSizeLimit) throws IssueTrackerContentLengthException {
        IssueContentLengthValidator issueContentLengthValidator = new IssueContentLengthValidator(titleSizeLimit, messageSizeLimit, messageSizeLimit);
        issueContentLengthValidator.validateContentLength(issueContentModel);
    }

    private IssueContentModel createIssueContentModel(int titleSizeLimit, int messageSizeLimit) throws AlertException {
        TestIssueMessageParser testIssueMessageParser = new TestIssueMessageParser(titleSizeLimit, messageSizeLimit);
        LinkableItem topic = new LinkableItem("topic", "https://example.topic.link");
        LinkableItem subtopic = new LinkableItem("subtopic", "https://example.subtopic.link");
        LinkableItem categoryItem = new LinkableItem("category-item", "category-link");

        ComponentItem arbitraryItem = new ComponentItem.Builder()
                                          .applyCategory("category")
                                          .applyOperation(ItemOperation.ADD)
                                          .applyComponentData("component-label", "component-value")
                                          .applyCategoryItem(categoryItem)
                                          .applyNotificationIds(Set.of(10L))
                                          .build();
        Set<ComponentItem> componentItems = Set.of(arbitraryItem);

        return testIssueMessageParser.createIssueContentModel("test-provider", IssueOperation.OPEN, topic, subtopic, componentItems, arbitraryItem);
    }

    private static class TestIssueMessageParser extends IssueTrackerMessageParser {
        protected TestIssueMessageParser(int titleSizeLimit, int messageSizeLimit) {
            super(titleSizeLimit, messageSizeLimit);
        }

        @Override
        protected String encodeString(String txt) {
            return txt;
        }

        @Override
        protected String emphasize(String txt) {
            return String.format("*%s*", txt);
        }

        @Override
        protected String createLink(String txt, String url) {
            return String.format(" [%s|%s] ", txt, url);
        }

        @Override
        protected String getLineSeparator() {
            return "\n";
        }
    }
}
