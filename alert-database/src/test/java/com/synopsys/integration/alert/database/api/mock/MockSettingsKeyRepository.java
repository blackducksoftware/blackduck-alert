package com.synopsys.integration.alert.database.api.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.synopsys.integration.alert.database.settings.SettingsKeyEntity;
import com.synopsys.integration.alert.database.settings.SettingsKeyRepository;

public class MockSettingsKeyRepository implements SettingsKeyRepository {

    Map<String, SettingsKeyEntity> settingsKeyEntities = new HashMap<>();
    Map<Long, SettingsKeyEntity> settingsKeyEntitiesById = new HashMap<>();

    @Override
    public Optional<SettingsKeyEntity> findByKey(String key) {
        return settingsKeyEntities.containsKey(key) ? Optional.of(settingsKeyEntities.get(key)) : Optional.empty();
    }

    @Override
    public List<SettingsKeyEntity> findAll() {
        return new ArrayList<SettingsKeyEntity>(settingsKeyEntities.values());
    }

    @Override
    public List<SettingsKeyEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<SettingsKeyEntity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<SettingsKeyEntity> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
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
    public void delete(SettingsKeyEntity entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends SettingsKeyEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends SettingsKeyEntity> S save(S entity) {
        settingsKeyEntities.put(entity.getKey(), entity);
        settingsKeyEntitiesById.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends SettingsKeyEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<SettingsKeyEntity> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends SettingsKeyEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<SettingsKeyEntity> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public SettingsKeyEntity getOne(Long aLong) {
        return null;
    }

    @Override
    public <S extends SettingsKeyEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends SettingsKeyEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends SettingsKeyEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends SettingsKeyEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends SettingsKeyEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends SettingsKeyEntity> boolean exists(Example<S> example) {
        return false;
    }
}
