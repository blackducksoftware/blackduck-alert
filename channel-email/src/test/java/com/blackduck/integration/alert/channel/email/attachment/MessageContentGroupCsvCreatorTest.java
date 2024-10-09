package com.blackduck.integration.alert.channel.email.attachment;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.ComponentItem;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.MessageContentGroup;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.ProviderMessageContent;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;

class MessageContentGroupCsvCreatorTest {
    @Test
    void createCsvStringTest() throws AlertException {
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

    @Test
    void createCsvStringWithCommasTest() throws AlertException {
        MessageContentGroup messageContentGroup = new MessageContentGroup();

        ProviderMessageContent.Builder providerMessageBuilder = new ProviderMessageContent.Builder();
        providerMessageBuilder.applyProvider("Example Provider", 1L, "Example Config");
        providerMessageBuilder.applyTopic("Example Topic Name", "Example Topic Value");

        ComponentItem.Builder componentItemBuilder = new ComponentItem.Builder();
        componentItemBuilder.applyNotificationId(1L);
        componentItemBuilder.applyOperation(ItemOperation.INFO);
        componentItemBuilder.applyCategory("Category");
        componentItemBuilder.applyComponentData("Component", "Component,With,Commas", "https://google.com");
        componentItemBuilder.applySubComponent("SubComponent", "SubComponent,With,Commas", "https://google.com");
        componentItemBuilder.applyCategoryItem("Example Category Item Name", "Example Category Item Value");
        componentItemBuilder.applyCategoryGroupingAttribute("CategoryGroupingAttributeName", "testGroupingName");

        providerMessageBuilder.applyComponentItem(componentItemBuilder.build());
        messageContentGroup.add(providerMessageBuilder.build());

        MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
        String csvString = messageContentGroupCsvCreator.createCsvString(messageContentGroup);
        assertNotNull(csvString);
    }
}
