package com.synopsys.integration.alert.processor.api.summarize;

import com.synopsys.integration.alert.processor.api.digest.model.ProviderMessage;

// TODO update this when we have completed models
public interface NotificationSummarizer {
    <T extends ProviderMessage<T>> T summarize(Object notification);

}
