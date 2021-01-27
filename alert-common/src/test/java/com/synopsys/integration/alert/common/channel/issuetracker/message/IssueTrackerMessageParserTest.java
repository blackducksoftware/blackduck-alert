package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

class IssueTrackerMessageParserTest {

    @Test
    void createIssueContentModel() throws AlertException {
        int titleSizeLimit = 10;
        int messageSizeLimit = 20;

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

        IssueContentModel issueContentModel = testIssueMessageParser.createIssueContentModel("test-provider", IssueOperation.OPEN, topic, subtopic, componentItems, arbitraryItem);

        IssueContentLengthValidator issueContentLengthValidator = new IssueContentLengthValidator(titleSizeLimit, messageSizeLimit, messageSizeLimit);
        issueContentLengthValidator.validateContentLength(issueContentModel);
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
