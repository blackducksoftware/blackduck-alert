/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.security.EncryptedRepositoryAccessor;
import com.synopsys.integration.alert.database.security.EncryptionUtility;

@Component
public class BlackDuckRepositoryAccessor extends EncryptedRepositoryAccessor {
    private final String API_TOKEN_FIELD_PROPERTY_KEY = "blackduck_api_key";
    private final GlobalBlackDuckRepository repository;

    @Autowired
    public BlackDuckRepositoryAccessor(final GlobalBlackDuckRepository repository, final EncryptionUtility encryptionUtility) {
        super(repository, encryptionUtility);
        this.repository = repository;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final GlobalBlackDuckConfigEntity blackDuckEntity = (GlobalBlackDuckConfigEntity) entity;
        final GlobalBlackDuckConfigEntity newEntity = new GlobalBlackDuckConfigEntity(blackDuckEntity.getBlackDuckTimeout(), encryptValue(API_TOKEN_FIELD_PROPERTY_KEY, blackDuckEntity.getBlackDuckApiKey()),
            blackDuckEntity.getBlackDuckUrl());
        newEntity.setId(blackDuckEntity.getId());
        return repository.save(newEntity);
    }

    @Override
    public DatabaseEntity decryptEntity(final DatabaseEntity entity) {
        final GlobalBlackDuckConfigEntity blackDuckEntity = (GlobalBlackDuckConfigEntity) entity;
        final Optional<String> decryptedValue = decryptValue(API_TOKEN_FIELD_PROPERTY_KEY, blackDuckEntity.getBlackDuckApiKey());
        final GlobalBlackDuckConfigEntity newEntity = new GlobalBlackDuckConfigEntity(blackDuckEntity.getBlackDuckTimeout(), decryptedValue.orElse(null), blackDuckEntity.getBlackDuckUrl());
        newEntity.setId(blackDuckEntity.getId());
        return newEntity;
    }
}
