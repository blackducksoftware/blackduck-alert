package com.synopsys.integration.alert.database.api.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.synopsys.integration.alert.database.DatabaseEntity;

public class MockAutoIncrementRepository<T extends DatabaseEntity> implements JpaRepository <T, Long> {
    private long currentId;
    private Map<Long,T> storedEntityMap;

    public static final <T extends DatabaseEntity> MockAutoIncrementRepository newInstance() {
        return new MockAutoIncrementRepository<T>();
    }

    protected MockAutoIncrementRepository() {
        this.currentId = 0;
        this.storedEntityMap = new HashMap<>();
    }

    private long incrementId() {
        return currentId++;
    }

    @Override
    public List<T> findAll() {
        return storedEntityMap.values().stream()
            .collect(Collectors.toList());
    }

    @Override
    public List<T> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<T> findAllById(Iterable<Long> ids) {
        List<T> entities = new LinkedList<>();
        Iterator<Long> iterator = ids.iterator();
        while(iterator.hasNext()) {
            Optional<T> entity = findById(iterator.next());
            entity.ifPresent(entities::add);
        }
        return entities;
    }

    @Override
    public long count() {
        return storedEntityMap.size();
    }

    @Override
    public void deleteById(Long id) {
        if(null != id && storedEntityMap.containsKey(id)) {
            storedEntityMap.remove(id);
        }
    }

    @Override
    public void delete(T entity) {
        Long key = storedEntityMap.entrySet().stream()
            .filter(entry -> entry.getValue().equals(entity))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
        deleteById(key);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        storedEntityMap.clear();
    }

    @Override
    public <S extends T> S save(S entity) {
        Long id = entity.getId();
        if(null == id) {
            entity.setId(incrementId());
        }
        storedEntityMap.put(id, entity);
        return entity;
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new LinkedList<>();
        Iterator<S> iterator = entities.iterator();
        while(iterator.hasNext()) {
            S savedEntity = save(iterator.next());
            savedEntities.add(savedEntity);
        }
        return savedEntities;
    }

    @Override
    public Optional<T> findById(Long id) {
        return Optional.ofNullable(storedEntityMap.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return storedEntityMap.containsKey(id);
    }

    @Override
    public void flush() {
        // nothing to be done
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        Iterator<T> iterator = entities.iterator();
        iterator.forEachRemaining(this::delete);
    }

    @Override
    public void deleteAllInBatch() {
        storedEntityMap.clear();
    }

    @Override
    public T getOne(Long id) {
        return storedEntityMap.get(id);
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return false;
    }
}
