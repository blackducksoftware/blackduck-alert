package com.synopsys.integration.alert.database.distribution.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

public class MockRepositoryContainer<ID extends Serializable, T extends Serializable> implements JpaRepository<T, ID> {
    private Map<ID, T> dataMap;
    private Function<T, ID> idGenerator;

    public MockRepositoryContainer(Function<T, ID> idGenerator) {
        dataMap = new ConcurrentHashMap<>();
        this.idGenerator = idGenerator;
    }

    protected Map<ID, T> getDataMap() {
        return dataMap;
    }

    protected Function<T, ID> getIdGenerator() {
        return idGenerator;
    }

    @Override
    public @NotNull List<T> findAll() {
        return new ArrayList<>(dataMap.values());
    }

    @Override
    public @NotNull List<T> findAll(@NotNull Sort sort) {
        return findAll();
    }

    @Override
    public @NotNull Page<T> findAll(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        List<List<T>> partitions = ListUtils.partition(new ArrayList<>(dataMap.values()), pageSize);
        List<T> pageData = List.of();
        for (int currentPage = 0; currentPage < partitions.size(); currentPage++) {
            if (currentPage == pageNumber) {
                pageData = partitions.get(currentPage);
            }
        }
        return new PageImpl<>(pageData, pageable, dataMap.size());
    }

    @Override
    public @NotNull List<T> findAllById(Iterable<ID> ids) {
        List<T> result = new ArrayList<>(dataMap.size());
        ids.forEach(id -> {
            if (dataMap.containsKey(id)) {
                result.add(dataMap.get(id));
            }
        });

        return result;
    }

    @Override
    public long count() {
        return dataMap.size();
    }

    @Override
    public void deleteById(@NotNull ID id) {
        dataMap.remove(id);
    }

    @Override
    public void delete(@NotNull T entity) {
        Optional<Map.Entry<ID, T>> storedEntity = dataMap.entrySet().stream()
            .filter(entry -> entry.getValue().equals(entity))
            .findFirst();

        storedEntity
            .map(Map.Entry::getKey)
            .ifPresent(dataMap::remove);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(dataMap::remove);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        dataMap.clear();
    }

    @Override
    public <S extends T> @NotNull S save(@NotNull S entity) {
        ID id = idGenerator.apply(entity);
        dataMap.put(id, entity);
        return (S) dataMap.get(id);
    }

    @Override
    public <S extends T> @NotNull List<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new LinkedList<>();
        for (S entity : entities) {
            savedEntities.add(save(entity));
        }
        return savedEntities;
    }

    @Override
    public @NotNull Optional<T> findById(@NotNull ID id) {
        return Optional.ofNullable(dataMap.get(id));
    }

    @Override
    public boolean existsById(@NotNull ID id) {
        return dataMap.containsKey(id);
    }

    @Override
    public void flush() {
        dataMap.clear();
    }

    @Override
    public <S extends T> S saveAndFlush(@NotNull S entity) {
        return save(entity);
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(@NotNull Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(@NotNull Iterable<T> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(@NotNull Iterable<ID> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        dataMap.clear();
    }

    @Override
    public T getOne(@NotNull ID id) {
        return dataMap.get(id);
    }

    @Override
    public T getById(@NotNull ID id) {
        return dataMap.get(id);
    }

    @Override
    public T getReferenceById(@NotNull ID id) {
        return dataMap.get(id);
    }

    @Override
    public <S extends T> @NotNull Optional<S> findOne(@NotNull Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends T> @NotNull List<S> findAll(@NotNull Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends T> @NotNull List<S> findAll(@NotNull Example<S> example, @NotNull Sort sort) {
        return List.of();
    }

    @Override
    public <S extends T> @NotNull Page<S> findAll(@NotNull Example<S> example, @NotNull Pageable pageable) {
        return Page.empty();
    }

    @Override
    public <S extends T> long count(@NotNull Example<S> example) {
        return 0;
    }

    @Override
    public <S extends T> boolean exists(@NotNull Example<S> example) {
        return false;
    }

    @Override
    public <S extends T, R> R findBy(@NotNull Example<S> example, @NotNull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
