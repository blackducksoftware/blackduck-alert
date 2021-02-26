/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import java.util.List;

import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;

public interface ProviderCallbackEventProducer {
    void sendProviderCallbackEvents(List<ProviderCallbackEvent> events);

}
