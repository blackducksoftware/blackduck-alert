/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.datasource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.BaseEntity;

public abstract class AbstractRepositoryWrapper<D extends BaseEntity, ID extends Serializable, R extends JpaRepository<D, ID>> {

    private final Logger logger;
    private final R repository;

    public AbstractRepositoryWrapper(final R repository) {
        this.repository = repository;
        logger = LoggerFactory.getLogger(getClass());
    }

    public Logger getLogger() {
        return logger;
    }

    public R getRepository() {
        return repository;
    }

    public long count() {
        return getRepository().count();
    }

    public boolean exists(final ID id) {
        return getRepository().exists(id);
    }

    public void delete(final ID id) {
        getRepository().delete(id);
    }

    public void delete(final D entity) {
        getRepository().delete(entity);
    }

    public void deleteAll() {
        getRepository().deleteAll();
    }

    public void delete(final Collection<D> entities) {
        getRepository().delete(entities);
    }

    public void deleteInBatch(final List<D> entities) {
        getRepository().deleteInBatch(entities);
    }

    public D findOne(final ID id) {
        try {
            final D entity = getRepository().findOne(id);
            return decryptSensitiveData(entity);
        } catch (final EncryptionException ex) {
            getLogger().error("Error getting entity ", ex);
            return null;
        }
    }

    public List<D> findAll(final List<ID> idList) {
        final List<D> entityList = getRepository().findAll(idList);
        if (entityList == null) {
            return Collections.emptyList();
        } else {
            try {
                final List<D> returnList = new ArrayList<>(entityList.size());
                for (final D entity : entityList) {
                    returnList.add(decryptSensitiveData(entity));
                }
                return returnList;
            } catch (final EncryptionException ex) {
                getLogger().error("Error finding all entities", ex);
                return Collections.emptyList();
            }
        }
    }

    public List<D> findAll() {
        final List<D> entityList = getRepository().findAll();
        if (entityList == null) {
            return Collections.emptyList();
        } else {
            try {
                final List<D> returnList = new ArrayList<>(entityList.size());
                for (final D entity : entityList) {
                    returnList.add(decryptSensitiveData(entity));
                }
                return returnList;
            } catch (final EncryptionException ex) {
                getLogger().error("Error finding all entities", ex);
                return Collections.emptyList();
            }
        }
    }

    public List<D> save(final List<D> entities) {
        if (entities != null) {
            final List<D> resultList = new ArrayList<>(entities.size());
            for (final D entity : entities) {
                resultList.add(save(entity));
            }
            return resultList;
        } else {
            return Collections.emptyList();
        }
    }

    public D save(final D entity) {
        try {
            final D encryptedEntity = encryptSensitiveData(entity);
            return getRepository().save(encryptedEntity);
        } catch (final EncryptionException ex) {
            getLogger().error("Error saving entity", ex);
            return null;
        }
    }

    public List<D> decryptSensitiveData(final List<D> entityList) {
        List<D> resultList;
        if (entityList == null) {
            resultList = Collections.emptyList();
        } else {
            try {
                resultList = new ArrayList<>(entityList.size());
                for (final D entity : entityList) {
                    resultList.add(decryptSensitiveData(entity));
                }
            } catch (final EncryptionException ex) {
                getLogger().error("Error transforming entity data", ex);
                resultList = Collections.emptyList();
            }
        }

        return resultList;
    }

    public abstract D encryptSensitiveData(D entity) throws EncryptionException;

    public abstract D decryptSensitiveData(D entity) throws EncryptionException;
}
