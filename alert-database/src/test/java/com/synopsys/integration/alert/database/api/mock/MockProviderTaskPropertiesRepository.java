package com.synopsys.integration.alert.database.api.mock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

public class MockProviderTaskPropertiesRepository implements ProviderTaskPropertiesRepository {

    private ProviderTaskPropertiesEntity providerTaskPropertiesEntity;

    public MockProviderTaskPropertiesRepository(Long providerConfigId, String taskName, String propertyName, String value) {
        providerTaskPropertiesEntity = new ProviderTaskPropertiesEntity(providerConfigId, taskName, propertyName, value);
    }

    @Override
    public Optional<ProviderTaskPropertiesEntity> findByTaskNameAndPropertyName(String taskName, String propertyName) {
        if (taskName == providerTaskPropertiesEntity.getTaskName() || propertyName == providerTaskPropertiesEntity.getPropertyName()) {
            return Optional.of(providerTaskPropertiesEntity);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ProviderTaskPropertiesEntity> findAll() {
        return null;
    }

    @Override
    public List<ProviderTaskPropertiesEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<ProviderTaskPropertiesEntity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<ProviderTaskPropertiesEntity> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(ProviderTaskPropertiesEntity entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends ProviderTaskPropertiesEntity> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ProviderTaskPropertiesEntity> findById(Long aLong) {
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
    public <S extends ProviderTaskPropertiesEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<ProviderTaskPropertiesEntity> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public ProviderTaskPropertiesEntity getOne(Long aLong) {
        return null;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> boolean exists(Example<S> example) {
        return false;
    }
}
