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
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;

// TODO fix SlackConfigEntity
public class MockSlackRepository implements JpaRepository<MockSlackRepository.SlackConfigEntity, Long> {

    // TODO remove this when Slack is on master
    public class SlackConfigEntity extends DatabaseEntity {
        private static final long serialVersionUID = 819426973456433591L;

    }

    private final Map<Long, SlackConfigEntity> mockDB;

    public MockSlackRepository() {
        this.mockDB = new LinkedHashMap<>();
    }

    @Override
    public List<SlackConfigEntity> findAll() {
        return (List<SlackConfigEntity>) mockDB.values();
    }

    @Override
    public List<SlackConfigEntity> findAll(final Sort sort) {
        return findAll();
    }

    @Override
    public List<SlackConfigEntity> findAll(final Iterable<Long> ids) {
        final List<SlackConfigEntity> matches = new ArrayList<>();
        ids.forEach(id -> {
            final SlackConfigEntity found = findOne(id);
            if (found != null) {
                matches.add(found);
            }
        });
        return matches;
    }

    @Override
    public <S extends SlackConfigEntity> List<S> save(final Iterable<S> entities) {
        entities.forEach(entity -> save(entity));
        return (List<S>) entities;
    }

    @Override
    public void flush() {
        // Unnecessary
    }

    @Override
    public <S extends SlackConfigEntity> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<SlackConfigEntity> entities) {
        delete(entities);
    }

    @Override
    public void deleteAllInBatch() {
        // Unused
    }

    @Override
    public SlackConfigEntity getOne(final Long id) {
        return mockDB.get(id);
    }

    @Override
    public <S extends SlackConfigEntity> List<S> findAll(final Example<S> example) {
        return (List<S>) findAll();
    }

    @Override
    public <S extends SlackConfigEntity> List<S> findAll(final Example<S> example, final Sort sort) {
        return (List<S>) findAll();
    }

    @Override
    public Page<SlackConfigEntity> findAll(final Pageable pageable) {
        return (Page<SlackConfigEntity>) findAll();
    }

    @Override
    public <S extends SlackConfigEntity> S save(final S entity) {
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
    public SlackConfigEntity findOne(final Long id) {
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
    public void delete(final SlackConfigEntity entity) {
        delete(entity.getId());
    }

    @Override
    public void delete(final Iterable<? extends SlackConfigEntity> entities) {
        mockDB.forEach((key, value) -> delete(key));
    }

    @Override
    public void deleteAll() {
        mockDB.clear();
    }

    @Override
    public <S extends SlackConfigEntity> S findOne(final Example<S> example) {
        // Unused
        return null;
    }

    @Override
    public <S extends SlackConfigEntity> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        // Unused
        return null;
    }

    @Override
    public <S extends SlackConfigEntity> long count(final Example<S> example) {
        // Unused
        return -1;
    }

    @Override
    public <S extends SlackConfigEntity> boolean exists(final Example<S> example) {
        // Unused
        return false;
    }
}
