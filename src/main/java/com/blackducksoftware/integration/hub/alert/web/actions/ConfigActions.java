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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class ConfigActions<D extends DatabaseEntity, R extends ConfigRestModel> {
    public final Class<D> databaseEntityClass;
    public final Class<R> configRestModelClass;
    public final JpaRepository<D, Long> repository;
    public final ObjectTransformer objectTransformer;

    public ConfigActions(final Class<D> databaseEntityClass, final Class<R> configRestModelClass, final JpaRepository<D, Long> repository, final ObjectTransformer objectTransformer) {
        this.databaseEntityClass = databaseEntityClass;
        this.configRestModelClass = configRestModelClass;
        this.repository = repository;
        this.objectTransformer = objectTransformer;
    }

    public boolean doesConfigExist(final String id) {
        return doesConfigExist(objectTransformer.stringToLong(id));
    }

    public boolean doesConfigExist(final Long id) {
        return id != null && repository.exists(id);
    }

    public List<R> getConfig(final Long id) throws IntegrationException {
        if (id != null) {
            final D foundEntity = repository.findOne(id);
            if (foundEntity != null) {
                final R restModel = objectTransformer.transformObject(foundEntity, configRestModelClass);
                if (restModel != null) {
                    return Arrays.asList(restModel);
                }
            }
            return Collections.emptyList();
        }
        final List<R> restModels = objectTransformer.transformObjects(repository.findAll(), configRestModelClass);
        if (restModels != null) {
            return restModels;
        }
        return Collections.emptyList();
    }

    public void deleteConfig(final String id) {
        deleteConfig(objectTransformer.stringToLong(id));
    }

    public void deleteConfig(final Long id) {
        repository.delete(id);
    }

    public D saveConfig(final R restModel) throws IntegrationException {
        if (restModel != null) {
            try {
                D createdEntity = objectTransformer.transformObject(restModel, databaseEntityClass);
                if (createdEntity != null) {
                    createdEntity = repository.save(createdEntity);
                }
                return createdEntity;
            } catch (final Exception e) {
                throw new IntegrationException();
            }
        }
        return null;
    }

    public abstract Map<String, String> validateConfig(R restModel);

    /**
     * If something needs to be triggered when the configuration is changed, this method should be overriden
     */
    public void configurationChangeTriggers(final R restModel) {

    }

}
