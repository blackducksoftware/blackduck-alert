package com.blackduck.integration.alert.api.channel.convert;

import java.util.List;

import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessage;

public interface ProviderMessageConverter<T extends ProviderMessage<T>> {
    List<String> convertToFormattedMessageChunks(T message, String jobName);

}
