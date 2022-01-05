/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import java.util.List;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public interface ProviderMessageConverter<T extends ProviderMessage<T>> {
    List<String> convertToFormattedMessageChunks(T message, String jobName);

}
