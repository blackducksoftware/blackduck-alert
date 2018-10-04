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
package com.synopsys.integration.alert.database.security;

import java.util.List;
import java.util.Optional;
import java.util.Vector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Transactional
public abstract class EncryptedRepositoryAccessor extends RepositoryAccessor {
    private final EncryptionUtility encryptionUtility;

    public EncryptedRepositoryAccessor(final JpaRepository<? extends DatabaseEntity, Long> repository, final EncryptionUtility encryptionUtility) {
        super(repository);
        this.encryptionUtility = encryptionUtility;
    }

    public String encryptValue(final String uniqueFieldNameKey, final String value) {
        return encryptionUtility.encrypt(uniqueFieldNameKey, value);
    }

    public Optional<String> decryptValue(final String uniqueFieldNameKey, final String value) {
        return encryptionUtility.decrypt(uniqueFieldNameKey, value);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<? extends DatabaseEntity> readEntities() {
        final List<? extends DatabaseEntity> entityList = super.readEntities();
        final List<DatabaseEntity> decryptedEntityList = new Vector<>(entityList.size());
        for (final DatabaseEntity entity : entityList) {
            decryptedEntityList.add(decryptEntity(entity));
        }

        return decryptedEntityList;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        final Optional<? extends DatabaseEntity> entity = super.readEntity(id);
        final Optional<? extends DatabaseEntity> decryptedEntity;
        if (entity.isPresent()) {
            decryptedEntity = Optional.of(decryptEntity(entity.get()));
        } else {
            decryptedEntity = Optional.empty();
        }

        return decryptedEntity;
    }

    public abstract DatabaseEntity decryptEntity(final DatabaseEntity entity);

}
