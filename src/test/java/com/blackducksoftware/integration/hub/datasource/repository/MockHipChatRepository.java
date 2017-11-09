/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.datasource.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HipChatRepository;

public class MockHipChatRepository implements HipChatRepository {
    private final Map<Long, HipChatConfigEntity> mockDB;

    public MockHipChatRepository() {
        this.mockDB = new LinkedHashMap<>();
    }

    @Override
    public List<HipChatConfigEntity> findAll() {
        return (List<HipChatConfigEntity>) mockDB.values();
    }

    @Override
    public List<HipChatConfigEntity> findAll(final Sort sort) {
        return findAll();
    }

    @Override
    public List<HipChatConfigEntity> findAll(final Iterable<Long> ids) {
        final List<HipChatConfigEntity> matches = new ArrayList<>();
        ids.forEach(id -> {
            final HipChatConfigEntity found = findOne(id);
            if (found != null) {
                matches.add(found);
            }
        });
        return matches;
    }

    @Override
    public <S extends HipChatConfigEntity> List<S> save(final Iterable<S> entities) {
        entities.forEach(entity -> save(entity));
        return (List<S>) entities;
    }

    @Override
    public void flush() {
        // Unnecessary
    }

    @Override
    public <S extends HipChatConfigEntity> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<HipChatConfigEntity> entities) {
        delete(entities);
    }

    @Override
    public void deleteAllInBatch() {
        // Unused
    }

    @Override
    public HipChatConfigEntity getOne(final Long id) {
        return mockDB.get(id);
    }

    @Override
    public <S extends HipChatConfigEntity> List<S> findAll(final Example<S> example) {
        return (List<S>) findAll();
    }

    @Override
    public <S extends HipChatConfigEntity> List<S> findAll(final Example<S> example, final Sort sort) {
        return (List<S>) findAll();
    }

    @Override
    public Page<HipChatConfigEntity> findAll(final Pageable pageable) {
        return (Page<HipChatConfigEntity>) findAll();
    }

    @Override
    public <S extends HipChatConfigEntity> S save(final S entity) {
        final Long lastIndex = new Long(mockDB.keySet().size());
        if (entity.getId() == null) {
            mockDB.put(lastIndex, entity);
            mockDB.get(lastIndex).setId(new Long(lastIndex));
        } else {
            mockDB.put(entity.getId(), entity);
        }
        return (S) mockDB.get(lastIndex);
    }

    @Override
    public HipChatConfigEntity findOne(final Long id) {
        return mockDB.get(id);
    }

    @Override
    public boolean exists(final Long id) {
        return mockDB.containsKey(id);
    }

    @Override
    public long count() {
        return mockDB.keySet().size();
    }

    @Override
    public void delete(final Long id) {
        mockDB.remove(id);
    }

    @Override
    public void delete(final HipChatConfigEntity entity) {
        delete(entity.getId());
    }

    @Override
    public void delete(final Iterable<? extends HipChatConfigEntity> entities) {
        mockDB.forEach((key, value) -> delete(key));
    }

    @Override
    public void deleteAll() {
        mockDB.clear();
    }

    @Override
    public <S extends HipChatConfigEntity> S findOne(final Example<S> example) {
        // Unused
        return null;
    }

    @Override
    public <S extends HipChatConfigEntity> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        // Unused
        return null;
    }

    @Override
    public <S extends HipChatConfigEntity> long count(final Example<S> example) {
        // Unused
        return -1;
    }

    @Override
    public <S extends HipChatConfigEntity> boolean exists(final Example<S> example) {
        // Unused
        return false;
    }

}
