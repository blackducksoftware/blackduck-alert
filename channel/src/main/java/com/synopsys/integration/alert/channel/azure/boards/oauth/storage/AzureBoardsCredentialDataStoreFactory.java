/**
 * channel
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
package com.synopsys.integration.alert.channel.azure.boards.oauth.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;

@Component
public class AzureBoardsCredentialDataStoreFactory extends AbstractDataStoreFactory {
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public AzureBoardsCredentialDataStoreFactory(ConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    /**
     * Returns a new instance of a {@link AzureBoardsCredentialDataStore} based on the given unique ID.
     *
     * <p>The {@link DataStore#getId()} must match the {@code id} parameter from this method.
     * @param id unique ID to refer to typed data store
     */
    @Override
    protected AzureBoardsCredentialDataStore createDataStore(String id) throws IOException {
        if (StoredCredential.DEFAULT_DATA_STORE_ID.equals(id)) {
            return new AzureBoardsCredentialDataStore(this, id, configurationAccessor);
        }
        throw new AlertRuntimeException("This factory can only manage an azure boards credential data store");
    }

}
