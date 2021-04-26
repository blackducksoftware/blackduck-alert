/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.convert;

import java.util.List;

import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public interface ProviderMessageConverter<T extends ProviderMessage<T>> {
    List<String> convertToFormattedMessageChunks(T message);

}
