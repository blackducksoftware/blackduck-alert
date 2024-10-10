/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.certificates.web;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.blackduck.integration.alert.database.certificates.ClientCertificateKeyEntity;
import com.blackduck.integration.alert.database.certificates.ClientCertificateKeyRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockClientCertificateKeyRepository extends MockRepositoryContainer<UUID, ClientCertificateKeyEntity> implements ClientCertificateKeyRepository {
    public MockClientCertificateKeyRepository() {
        super(ClientCertificateKeyEntity::getId);
    }

    @Override
    public @NotNull ClientCertificateKeyEntity save(@NotNull ClientCertificateKeyEntity entity) {
        // For the case where id is null; the db generates an id for us.
        // Since this mock data map doesn't do so, we will generate the id here.
        UUID id = getIdGenerator().apply(entity);
        if (id == null) {
            id = UUID.randomUUID();
            entity.setId(id);
        }
        getDataMap().put(id, entity);
        return getDataMap().get(id);
    }

    @Override
    public Optional<ClientCertificateKeyEntity> findByName(String name) {
        return findAll()
                .stream()
                .filter(entity -> entity.getName().equals(name))
                .findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        return findAll()
                .stream()
                .anyMatch(entity -> entity.getName().equals(name));
    }

    @Override
    public void deleteByName(String name) {
        findByName(name)
                .map(ClientCertificateKeyEntity::getId)
                .ifPresent(this::deleteById);
    }
}
