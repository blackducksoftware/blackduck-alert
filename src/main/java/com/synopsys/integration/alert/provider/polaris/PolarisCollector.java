package com.synopsys.integration.alert.provider.polaris;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.polaris.model.AlertPolarisNotificationTypeEnum;

@Component
public class PolarisCollector extends MessageContentCollector {

    public PolarisCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList) {
        super(jsonExtractor, messageContentProcessorList, List.of(PolarisProviderContentTypes.ISSUE_COUNT_INCREASED, PolarisProviderContentTypes.ISSUE_COUNT_DECREASED));
    }

    @Override
    protected void addCategoryItems(final List<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final Optional<JsonField<Integer>> optionalCountField = getIntegerFields(notificationFields)
                                                                    .stream()
                                                                    .findFirst();
        if (optionalCountField.isPresent()) {
            final JsonField<Integer> countField = optionalCountField.get();
            final Integer currentCount = jsonFieldAccessor.getFirst(countField).orElse(0);
            final ItemOperation operation = getOperationFromNotificationType(notificationContent.getNotificationType());

            final LinkableItem countItem = new LinkableItem(countField.getLabel(), currentCount.toString());
            final CategoryKey key = CategoryKey.from(notificationContent.getNotificationType(), notificationContent.getId().toString());
            categoryItems.add(new CategoryItem(key, operation, notificationContent.getId(), countItem));
        }
    }

    private ItemOperation getOperationFromNotificationType(final String notificationType) {
        if (AlertPolarisNotificationTypeEnum.ISSUE_COUNT_INCREASED.name().equals(notificationType)) {
            return ItemOperation.ADD;
        }
        return ItemOperation.DELETE;
    }
}
