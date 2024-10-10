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

import com.blackduck.integration.alert.database.certificates.ClientCertificateEntity;
import com.blackduck.integration.alert.database.certificates.ClientCertificateRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockClientCertificateRepository extends MockRepositoryContainer<UUID, ClientCertificateEntity> implements ClientCertificateRepository {
    public MockClientCertificateRepository() {
        super(ClientCertificateEntity::getId);
    }

    @Override
    public @NotNull ClientCertificateEntity save(@NotNull ClientCertificateEntity entity) {
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
    public Optional<ClientCertificateEntity> findByAlias(String alias) {
        return findAll()
                .stream()
                .filter(entity -> entity.getAlias().equals(alias))
                .findFirst();
    }

    @Override
    public boolean existsByAlias(String alias) {
        return findAll()
                .stream()
                .anyMatch(entity -> entity.getAlias().equals(alias));
    }

    @Override
    public void deleteByAlias(String alias) {
        findByAlias(alias)
                .map(ClientCertificateEntity::getId)
                .ifPresent(this::deleteById);
    }
}
