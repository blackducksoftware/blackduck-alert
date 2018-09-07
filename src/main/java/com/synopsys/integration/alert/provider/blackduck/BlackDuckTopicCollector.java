package com.synopsys.integration.alert.provider.blackduck;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;

@Component
public class BlackDuckTopicCollector extends TopicCollector {

    @Autowired
    public BlackDuckTopicCollector(final JsonExtractor jsonExtractor, final BlackDuckDescriptor blackDuckDescriptor) {
        super(jsonExtractor, blackDuckDescriptor);
    }

    @Override
    public void insert(final NotificationContent notification) {
        final TopicContent content = getContentOrCreateIfDoesNotExist(notification);

        final List<CategoryItem> categoryItems = content.getCategoryItemList();
        addCategoryItems(categoryItems);

        addContent(content);
    }

    @Override
    public List<TopicContent> collect(final FormatType format) {
        // TODO implement
        return getCopyOfCollectedContent();
    }

    private void addCategoryItems(final List<CategoryItem> categoryItems) {
        // TODO implement
    }
}
