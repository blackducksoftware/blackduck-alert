/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api.mock;

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

import com.blackduck.integration.alert.database.settings.SettingsKeyEntity;
import com.blackduck.integration.alert.database.settings.SettingsKeyRepository;

public class MockSettingsKeyRepository extends DefaultMockJPARepository<SettingsKeyEntity, Long> implements SettingsKeyRepository {

    private Map<String, SettingsKeyEntity> settingsKeyEntities = new HashMap<>();
    private Map<Long, SettingsKeyEntity> settingsKeyEntitiesById = new HashMap<>();

    @Override
    public Optional<SettingsKeyEntity> findByKey(String key) {
        return settingsKeyEntities.containsKey(key) ? Optional.of(settingsKeyEntities.get(key)) : Optional.empty();
    }

    @Override
    public List<SettingsKeyEntity> findAll() {
        return new ArrayList<>(settingsKeyEntities.values());
    }

    @Override
    public void deleteById(Long aLong) {
        if (settingsKeyEntitiesById.containsKey(aLong)) {
            String key = settingsKeyEntitiesById.get(aLong).getKey();
            settingsKeyEntities.remove(key);
            settingsKeyEntitiesById.remove(aLong);
        }
    }

    @Override
    public <S extends SettingsKeyEntity> S save(S entity) {
        settingsKeyEntities.put(entity.getKey(), entity);
        settingsKeyEntitiesById.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends SettingsKeyEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> savedItems = new LinkedList<>();
        Iterator<S> iterator = entities.iterator();
        while (iterator.hasNext()) {
            S entity = iterator.next();
            savedItems.add(save(entity));
        }
        return savedItems;
    }

    @Override
    public void deleteAllInBatch(Iterable<SettingsKeyEntity> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        longs.forEach(this::deleteById);

    }

    @Override
    public SettingsKeyEntity getById(Long aLong) {
        return findById(aLong).orElse(null);
    }

    @Override
    public SettingsKeyEntity getReferenceById(Long aLong) {
        return getById(aLong);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }

    @Override
    public <S extends SettingsKeyEntity, R> R findBy(
        Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction
    ) {
        return null;
    }
}
