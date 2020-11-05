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
package com.synopsys.integration.alert.common.provider;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;

public abstract class Provider {
    private final ProviderKey key;
    private final ProviderContent providerContent;

    public Provider(ProviderKey key, ProviderContent providerContent) {
        this.key = key;
        this.providerContent = providerContent;
    }

    public ProviderKey getKey() {
        return key;
    }

    public abstract boolean validate(ConfigurationModel configurationModel);

    public abstract StatefulProvider createStatefulProvider(ConfigurationModel configurationModel) throws AlertException;

    public abstract ProviderNotificationClassMap getNotificationClassMap();

    public ProviderContent getProviderContent() {
        return providerContent;
    }

}
