package com.synopsys.integration.alert.email;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.email.MessageContentGroupCsvCreator;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public class MessageContentGroupCsvCreatorTest {
    @Test
    public void createCsvStringTest() throws AlertException {
        MessageContentGroup messageContentGroup = new MessageContentGroup();

        ProviderMessageContent.Builder providerMessageBuilder = new ProviderMessageContent.Builder();
        providerMessageBuilder.applyProvider("Example Provider", 1L, "Example Config");
        providerMessageBuilder.applyTopic("Example Topic Name", "Example Topic Value");

        ComponentItem.Builder componentItemBuilder = new ComponentItem.Builder();
        componentItemBuilder.applyNotificationId(1L);
        componentItemBuilder.applyOperation(ItemOperation.INFO);
        componentItemBuilder.applyCategory("Example Category");
        componentItemBuilder.applyComponentData("Example Component Name", "Example Component Value", "https://google.com");
        componentItemBuilder.applySubComponent("Example SubComponent Name", "Example SubComponent Value", "https://google.com");
        componentItemBuilder.applyCategoryItem("Example Category Item Name", "Example Category Item Value");
        componentItemBuilder.applyCategoryGroupingAttribute("Example Category Grouping Attribute Name", "Example Category Grouping Attribute Value");

        providerMessageBuilder.applyComponentItem(componentItemBuilder.build());
        messageContentGroup.add(providerMessageBuilder.build());

        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        String csvString = messageContentGroupCsvCreator.createCsvString(messageContentGroup);
        assertNotNull(csvString);
    }

}
