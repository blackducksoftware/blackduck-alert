package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;

public abstract class BlackDuckTopicCollector extends TopicCollector {

    public BlackDuckTopicCollector(final JsonExtractor jsonExtractor, final BlackDuckDescriptor blackDuckDescriptor) {
        super(jsonExtractor, blackDuckDescriptor);
    }

    @Override
    public void insert(final NotificationContent notification) {
        final List<TopicContent> contents = getContentsOrCreateIfDoesNotExist(notification);
        for (final TopicContent content : contents) {
            addCategoryItemsToContent(content, notification);
            addContent(content);
        }
    }

    @Override
    public List<TopicContent> collect(final FormatType format) {
        // TODO implement
        return getCopyOfCollectedContent();
    }

    protected abstract void addCategoryItemsToContent(final TopicContent content, final NotificationContent notification);

    protected abstract ItemOperation getOperationFromNotification(final NotificationContent notification);
}
