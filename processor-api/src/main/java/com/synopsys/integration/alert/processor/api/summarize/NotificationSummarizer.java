package com.synopsys.integration.alert.processor.api.summarize;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;

public interface NotificationSummarizer {
    SimpleMessage summarize(ProviderMessage<?> digestedNotification);

}
