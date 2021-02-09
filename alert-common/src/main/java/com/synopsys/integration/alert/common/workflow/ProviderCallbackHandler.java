/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.AlertEventListener;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ProviderCallbackHandler extends MessageReceiver<ProviderCallbackEvent> implements AlertEventListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Provider provider;
    private final ConfigurationAccessor configurationAccessor;

    public ProviderCallbackHandler(Provider provider, ConfigurationAccessor configurationAccessor, Gson gson) {
        super(gson, ProviderCallbackEvent.class);
        this.provider = provider;
        this.configurationAccessor = configurationAccessor;
    }

    public final ProviderKey getProviderKey() {
        return provider.getKey();
    }

    @Override
    public final void handleEvent(ProviderCallbackEvent event) {
        if (event.getDestination().equals(getProviderKey().getUniversalKey())) {
            try {
                Optional<StatefulProvider> optionalStatefulProvider = validateAndRetrieveStatefulProvider(event);
                if (optionalStatefulProvider.isPresent()) {
                    performProviderCallback(event, optionalStatefulProvider.get());
                } else {
                    logger.warn("Cannot perform callback due to invalid provider config");
                }
            } catch (IntegrationException ex) {
                logger.error("There was an error performing the callback.", ex);
            }
        } else {
            logger.warn("Received an event for provider '{}', but this provider is '{}' with key '{}'.", event.getDestination(), getProviderKey().getDisplayName(), getProviderKey().getUniversalKey());
        }
    }

    @Override
    public final String getDestinationName() {
        return getProviderKey().getUniversalKey();
    }

    protected abstract void performProviderCallback(ProviderCallbackEvent event, StatefulProvider statefulProvider) throws IntegrationException;

    private Optional<StatefulProvider> validateAndRetrieveStatefulProvider(ProviderCallbackEvent event) throws IntegrationException {
        ContentKey contentKey = event.getProviderContentKey();
        Optional<ConfigurationModel> optionalProviderConfigModel = configurationAccessor.getConfigurationById(contentKey.getProviderConfigId());
        if (optionalProviderConfigModel.isPresent()) {
            ConfigurationModel providerGlobalConfig = optionalProviderConfigModel.get();
            boolean isGlobalConfigValid = provider.validate(providerGlobalConfig);
            if (isGlobalConfigValid) {
                return Optional.of(provider.createStatefulProvider(providerGlobalConfig));
            }
            logger.warn("The provider config with id '{}' for the callback event with id '{}' is invalid.", contentKey.getProviderConfigId(), event.getEventId());
        } else {
            logger.warn("The provider config with id '{}' for the callback event with id '{}' no longer exists.", contentKey.getProviderConfigId(), event.getEventId());
        }
        return Optional.empty();
    }

}
