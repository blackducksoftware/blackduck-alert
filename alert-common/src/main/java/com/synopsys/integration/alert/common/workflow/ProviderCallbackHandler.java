/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ProviderCallbackHandler extends MessageReceiver<ProviderCallbackEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProviderKey providerKey;

    public ProviderCallbackHandler(Gson gson, ProviderKey providerKey) {
        super(gson, ProviderCallbackEvent.class);
        this.providerKey = providerKey;
    }

    public ProviderKey getProviderKey() {
        return providerKey;
    }

    @Override
    public void handleEvent(ProviderCallbackEvent event) {
        if (event.getDestination().equals(providerKey.getUniversalKey())) {
            try {
                performProviderCallback(event);
            } catch (IntegrationException ex) {
                logger.error("There was an error performing the callback.", ex);
            }
        } else {
            logger.warn("Received an event for provider '{}', but this provider is '{}' with key '{}'.", event.getDestination(), providerKey.getDisplayName(), providerKey.getUniversalKey());
        }
    }

    protected abstract void performProviderCallback(ProviderCallbackEvent event) throws IntegrationException;

}
