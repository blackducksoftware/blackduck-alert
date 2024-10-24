/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api.mock;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;

import com.blackduck.integration.alert.database.certificates.CustomCertificateEntity;
import com.blackduck.integration.alert.database.certificates.CustomCertificateRepository;

public class MockCustomCertificateRepository extends DefaultMockJPARepository<CustomCertificateEntity, Long> implements CustomCertificateRepository {

    private Map<Long, CustomCertificateEntity> customCertificateEntityMapById = new HashMap<>();

    private long currentId = 0;

    public MockCustomCertificateRepository() {

    }

    public MockCustomCertificateRepository(String alias, String content, OffsetDateTime lastUpdated) {
        CustomCertificateEntity customCertificateEntity = new CustomCertificateEntity(alias, content, lastUpdated);
        this.save(customCertificateEntity);
    }

    @Override
    public Optional<CustomCertificateEntity> findByAlias(String alias) {
        return customCertificateEntityMapById.values()
                   .stream()
                   .filter(entity -> entity.getAlias().equals(alias))
                   .findFirst();
    }

    @Override
    public List<CustomCertificateEntity> findAll() {
        return new ArrayList<>(customCertificateEntityMapById.values());
    }

    @Override
    public Optional<CustomCertificateEntity> findById(Long id) {
        return Optional.ofNullable(customCertificateEntityMapById.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return customCertificateEntityMapById.containsKey(id);
    }

    @Override
    public <S extends CustomCertificateEntity> S save(S entity) {
        entity.setId(currentId);
        customCertificateEntityMapById.put(currentId, entity);
        currentId++;
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        customCertificateEntityMapById.remove(id);
    }

    @Override
    public <S extends CustomCertificateEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> savedItems = new LinkedList<>();
        Iterator<S> iterator = entities.iterator();
        while (iterator.hasNext()) {
            S entity = iterator.next();
            savedItems.add(save(entity));
        }
        return savedItems;
    }

    @Override
    public void deleteAllInBatch(Iterable<CustomCertificateEntity> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        longs.forEach(this::deleteById);

    }

    @Override
    public CustomCertificateEntity getById(Long aLong) {
        return findById(aLong).orElse(null);
    }

    @Override
    public CustomCertificateEntity getReferenceById(Long aLong) {
        return getById(aLong);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }

    @Override
    public <S extends CustomCertificateEntity, R> R findBy(
        Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction
    ) {
        return null;
    }
}
