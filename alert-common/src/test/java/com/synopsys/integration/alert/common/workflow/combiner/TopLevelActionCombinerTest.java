package com.synopsys.integration.alert.common.workflow.combiner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public class TopLevelActionCombinerTest {
    @Test
    public void combineWithSubTopicTest() throws AlertException {
        TopLevelActionCombiner topLevelActionCombiner = new TopLevelActionCombiner();

        String commonProviderValue = "common_provider";
        Long commonProviderId = 1L;
        String commonTopicName = "common topic name";
        String commonTopicValue = "common topic value";
        String commonSubTopicName = "common_sub_topic_name";
        String commonSubTopicValue = "common.sub.topic.value";

        ProviderMessageContent addAction = new ProviderMessageContent.Builder()
                                               .applyNotificationId(1L)
                                               .applyProvider(commonProviderValue, commonProviderId)
                                               .applyTopic(commonTopicName, commonTopicValue)
                                               .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                               .applyAction(ItemOperation.ADD)
                                               .build();

        ProviderMessageContent updateAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                  .applyAction(ItemOperation.UPDATE)
                                                  .build();

        ProviderMessageContent infoAction = new ProviderMessageContent.Builder()
                                                .applyNotificationId(1L)
                                                .applyProvider(commonProviderValue, commonProviderId)
                                                .applyTopic(commonTopicName, commonTopicValue)
                                                .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                .applyAction(ItemOperation.INFO)
                                                .build();

        ProviderMessageContent unrelatedTopicAdd = new ProviderMessageContent.Builder()
                                                       .applyNotificationId(1L)
                                                       .applyProvider(commonProviderValue, commonProviderId)
                                                       .applyTopic("uncommon topic name 123", "random value")
                                                       .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                       .applyAction(ItemOperation.ADD)
                                                       .build();

        ProviderMessageContent deleteAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                  .applyAction(ItemOperation.DELETE)
                                                  .build();

        List<ProviderMessageContent> orderedActionMessages = List.of(addAction, updateAction, infoAction, unrelatedTopicAdd, deleteAction);
        List<ProviderMessageContent> combinedMessages = topLevelActionCombiner.combine(orderedActionMessages);
        assertEquals(1, combinedMessages.size());
    }

    @Test
    public void combineWithoutSubTopicTest() throws AlertException {
        TopLevelActionCombiner topLevelActionCombiner = new TopLevelActionCombiner();

        String commonProviderValue = "common_provider";
        Long commonProviderId = 1L;
        String commonTopicName = "common topic name";
        String commonTopicValue = "common topic value";
        String commonSubTopicName = "common_sub_topic_name";
        String commonSubTopicValue = "common.sub.topic.value";

        ProviderMessageContent addAction = new ProviderMessageContent.Builder()
                                               .applyNotificationId(1L)
                                               .applyProvider(commonProviderValue, commonProviderId)
                                               .applyTopic(commonTopicName, commonTopicValue)
                                               .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                               .applyAction(ItemOperation.ADD)
                                               .build();

        ProviderMessageContent updateAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                  .applyAction(ItemOperation.UPDATE)
                                                  .build();

        ProviderMessageContent infoAction = new ProviderMessageContent.Builder()
                                                .applyNotificationId(1L)
                                                .applyProvider(commonProviderValue, commonProviderId)
                                                .applyTopic(commonTopicName, commonTopicValue)
                                                .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                .applyAction(ItemOperation.INFO)
                                                .build();

        ProviderMessageContent unrelatedTopicAdd = new ProviderMessageContent.Builder()
                                                       .applyNotificationId(1L)
                                                       .applyProvider(commonProviderValue, commonProviderId)
                                                       .applyTopic("uncommon topic name 123", "random value")
                                                       .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                       .applyAction(ItemOperation.ADD)
                                                       .build();

        ProviderMessageContent deleteAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applyAction(ItemOperation.DELETE)
                                                  .build();

        List<ProviderMessageContent> orderedActionMessages = List.of(addAction, updateAction, infoAction, unrelatedTopicAdd, deleteAction);
        List<ProviderMessageContent> combinedMessages = topLevelActionCombiner.combine(orderedActionMessages);
        assertEquals(1, combinedMessages.size());
    }

    @Test
    public void combineWithoutSubTopicOnSomeTest() throws AlertException {
        TopLevelActionCombiner topLevelActionCombiner = new TopLevelActionCombiner();

        String commonProviderValue = "common_provider";
        Long commonProviderId = 1L;
        String commonTopicName = "common topic name";
        String commonTopicValue = "common topic value";
        String commonSubTopicName = "common_sub_topic_name";
        String commonSubTopicValue = "common.sub.topic.value";

        ProviderMessageContent addAction = new ProviderMessageContent.Builder()
                                               .applyNotificationId(1L)
                                               .applyProvider(commonProviderValue, commonProviderId)
                                               .applyTopic(commonTopicName, commonTopicValue)
                                               .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                               .applyAction(ItemOperation.ADD)
                                               .build();

        ProviderMessageContent updateAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applyAction(ItemOperation.UPDATE)
                                                  .build();

        ProviderMessageContent infoAction = new ProviderMessageContent.Builder()
                                                .applyNotificationId(1L)
                                                .applyProvider(commonProviderValue, commonProviderId)
                                                .applyTopic(commonTopicName, commonTopicValue)
                                                .applyAction(ItemOperation.INFO)
                                                .build();

        ProviderMessageContent unrelatedTopicAdd = new ProviderMessageContent.Builder()
                                                       .applyNotificationId(1L)
                                                       .applyProvider(commonProviderValue, commonProviderId)
                                                       .applyTopic("uncommon topic name 123", "random value")
                                                       .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                       .applyAction(ItemOperation.ADD)
                                                       .build();

        ProviderMessageContent deleteAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                  .applyAction(ItemOperation.DELETE)
                                                  .build();

        List<ProviderMessageContent> orderedActionMessages = List.of(addAction, updateAction, infoAction, unrelatedTopicAdd, deleteAction);
        List<ProviderMessageContent> combinedMessages = topLevelActionCombiner.combine(orderedActionMessages);
        assertEquals(3, combinedMessages.size());
    }

    @Test
    public void combineWithComponentItemsTest() throws AlertException {
        TopLevelActionCombiner topLevelActionCombiner = new TopLevelActionCombiner();

        String commonProviderValue = "common_provider";
        Long commonProviderId = 1L;
        String commonTopicName = "common topic name";
        String commonTopicValue = "common topic value";
        String commonSubTopicName = "common_sub_topic_name";
        String commonSubTopicValue = "common.sub.topic.value";

        ComponentItem componentItem = new ComponentItem.Builder()
                                          .applyCategory("example")
                                          .applyOperation(ItemOperation.INFO)
                                          .applyComponentData("exmaple", "example")
                                          .applyCategoryItem("examle", "example")
                                          .applyNotificationId(1L)
                                          .build();

        ProviderMessageContent addAction = new ProviderMessageContent.Builder()
                                               .applyNotificationId(1L)
                                               .applyProvider(commonProviderValue, commonProviderId)
                                               .applyTopic(commonTopicName, commonTopicValue)
                                               .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                               .applyAction(ItemOperation.ADD)
                                               .build();

        ProviderMessageContent updateAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                  .applyComponentItem(componentItem)
                                                  .applyAction(ItemOperation.UPDATE)
                                                  .build();

        ProviderMessageContent infoAction = new ProviderMessageContent.Builder()
                                                .applyNotificationId(1L)
                                                .applyProvider(commonProviderValue, commonProviderId)
                                                .applyTopic(commonTopicName, commonTopicValue)
                                                .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                .applyComponentItem(componentItem)
                                                .applyAction(ItemOperation.INFO)
                                                .build();

        ProviderMessageContent unrelatedTopicAdd = new ProviderMessageContent.Builder()
                                                       .applyNotificationId(1L)
                                                       .applyProvider(commonProviderValue, commonProviderId)
                                                       .applyTopic("uncommon topic name 123", "random value")
                                                       .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                       .applyAction(ItemOperation.ADD)
                                                       .build();

        ProviderMessageContent deleteAction = new ProviderMessageContent.Builder()
                                                  .applyNotificationId(1L)
                                                  .applyProvider(commonProviderValue, commonProviderId)
                                                  .applyTopic(commonTopicName, commonTopicValue)
                                                  .applySubTopic(commonSubTopicName, commonSubTopicValue)
                                                  .applyAction(ItemOperation.DELETE)
                                                  .build();

        List<ProviderMessageContent> orderedActionMessages = List.of(addAction, updateAction, infoAction, unrelatedTopicAdd, deleteAction);
        List<ProviderMessageContent> combinedMessages = topLevelActionCombiner.combine(orderedActionMessages);
        assertEquals(3, combinedMessages.size());
    }

}
