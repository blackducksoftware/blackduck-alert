package com.synopsys.integration.alert.processor.api.extract;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;
import com.synopsys.integration.alert.processor.api.filter.FilterableNotificationWrapper;

public interface NotificationExtractor {
    <T extends ProviderMessage<T>> ProviderMessage<T> extract(FilterableNotificationWrapper<?> filteredNotification);

}
