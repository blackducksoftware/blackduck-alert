/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.convert;

import java.util.List;

import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessage;

public interface ProviderMessageConverter<T extends ProviderMessage<T>> {
    List<String> convertToFormattedMessageChunks(T message, String jobName);

}
