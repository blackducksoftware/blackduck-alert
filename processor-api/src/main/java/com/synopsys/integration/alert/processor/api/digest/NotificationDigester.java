package com.synopsys.integration.alert.processor.api.digest;

import java.util.List;

import com.synopsys.integration.alert.processor.api.digest.model.ProviderMessage;

public interface NotificationDigester {
    <T extends ProviderMessage<T>> List<T> digest(List<Object> notifications);

}
