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

import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;

public class MockEmailRepository implements EmailRepository {
    private final Map<Long, EmailConfigEntity> mockDB;

    public MockEmailRepository() {
        this.mockDB = new LinkedHashMap<>();
    }

    @Override
    public List<EmailConfigEntity> findAll() {
        return (List<EmailConfigEntity>) mockDB.values();
    }

    @Override
    public List<EmailConfigEntity> findAll(final Sort sort) {
        return findAll();
    }

    @Override
    public List<EmailConfigEntity> findAll(final Iterable<Long> ids) {
        final List<EmailConfigEntity> matches = new ArrayList<>();
        ids.forEach(id -> {
            final EmailConfigEntity found = findOne(id);
            if (found != null) {
                matches.add(found);
            }
        });
        return matches;
    }

    @Override
    public <S extends EmailConfigEntity> List<S> save(final Iterable<S> entities) {
        entities.forEach(entity -> save(entity));
        return (List<S>) entities;
    }

    @Override
    public void flush() {
        // Unnecessary
    }

    @Override
    public <S extends EmailConfigEntity> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<EmailConfigEntity> entities) {
        delete(entities);
    }

    @Override
    public void deleteAllInBatch() {
        // Unused
    }

    @Override
    public EmailConfigEntity getOne(final Long id) {
        return mockDB.get(id);
    }

    @Override
    public <S extends EmailConfigEntity> List<S> findAll(final Example<S> example) {
        return (List<S>) findAll();
    }

    @Override
    public <S extends EmailConfigEntity> List<S> findAll(final Example<S> example, final Sort sort) {
        return (List<S>) findAll();
    }

    @Override
    public Page<EmailConfigEntity> findAll(final Pageable pageable) {
        return (Page<EmailConfigEntity>) findAll();
    }

    @Override
    public <S extends EmailConfigEntity> S save(final S entity) {
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
    public EmailConfigEntity findOne(final Long id) {
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
    public void delete(final EmailConfigEntity entity) {
        delete(entity.getId());
    }

    @Override
    public void delete(final Iterable<? extends EmailConfigEntity> entities) {
        mockDB.forEach((key, value) -> delete(key));
    }

    @Override
    public void deleteAll() {
        mockDB.clear();
    }

    @Override
    public <S extends EmailConfigEntity> S findOne(final Example<S> example) {
        // Unused
        return null;
    }

    @Override
    public <S extends EmailConfigEntity> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        // Unused
        return null;
    }

    @Override
    public <S extends EmailConfigEntity> long count(final Example<S> example) {
        // Unused
        return -1;
    }

    @Override
    public <S extends EmailConfigEntity> boolean exists(final Example<S> example) {
        // Unused
        return false;
    }

}
