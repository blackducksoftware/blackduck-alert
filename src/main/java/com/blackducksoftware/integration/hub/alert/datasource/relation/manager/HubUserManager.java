/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.datasource.relation.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DatabaseChannelRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserEmailRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserFrequenciesRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelationPK;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserSlackRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserEmailRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserFrequenciesRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserHipChatRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserProjectVersionsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserSlackRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.HubUsersConfigWrapperActions;
import com.blackducksoftware.integration.hub.alert.web.model.HubUsersConfigWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.ProjectVersionConfigWrapper;

@Component
public class HubUserManager {
    private final Logger logger = LoggerFactory.getLogger(HubUsersConfigWrapperActions.class);

    private final HubUsersRepository hubUsersRepository;
    private final HubUserFrequenciesRepository hubUserFrequenciesRepository;
    private final HubUserEmailRepository hubUserEmailRepository;
    private final HubUserHipChatRepository hubUserHipChatRepository;
    private final HubUserSlackRepository hubUserSlackRepository;
    private final HubUserProjectVersionsRepository hubUserProjectVersionsRepository;
    private final ObjectTransformer objectTransformer;

    @Autowired
    public HubUserManager(final HubUsersRepository hubUsersRepository, final HubUserFrequenciesRepository hubUserFrequenciesRepository, final HubUserEmailRepository hubUserEmailRepository,
            final HubUserHipChatRepository hubUserHipChatRepository, final HubUserSlackRepository hubUserSlackRepository, final HubUserProjectVersionsRepository hubUserProjectVersionsRepository, final ObjectTransformer objectTransformer) {
        this.hubUsersRepository = hubUsersRepository;
        this.hubUserFrequenciesRepository = hubUserFrequenciesRepository;
        this.hubUserEmailRepository = hubUserEmailRepository;
        this.hubUserHipChatRepository = hubUserHipChatRepository;
        this.hubUserSlackRepository = hubUserSlackRepository;
        this.hubUserProjectVersionsRepository = hubUserProjectVersionsRepository;
        this.objectTransformer = objectTransformer;
    }

    public Long saveConfig(final HubUsersConfigWrapper wrapper) throws AlertException {
        HubUsersEntity hubUsersEntity;
        Long configId = objectTransformer.stringToLong(wrapper.getId());
        if (configId == null || !hubUsersRepository.exists(configId)) {
            hubUsersEntity = hubUsersRepository.save(new HubUsersEntity(wrapper.getUsername(), objectTransformer.stringToBoolean(wrapper.getExistsOnHub())));
        } else {
            hubUsersEntity = hubUsersRepository.findOne(configId);
        }
        configId = hubUsersEntity.getId();
        saveFrequency(configId, wrapper.getFrequency());
        saveChannelId(configId, wrapper.getEmailConfigId(), hubUserEmailRepository, HubUserEmailRelation.class);
        saveChannelId(configId, wrapper.getHipChatConfigId(), hubUserHipChatRepository, HubUserHipChatRelation.class);
        saveChannelId(configId, wrapper.getSlackConfigId(), hubUserSlackRepository, HubUserSlackRelation.class);
        saveProjectVersions(configId, wrapper.getProjectVersions());

        return configId;
    }

    public void deleteConfig(final String id) {
        deleteConfig(objectTransformer.stringToLong(id));
    }

    public void deleteConfig(final Long id) {
        if (doesConfigExist(id)) {
            hubUsersRepository.delete(id);
            hubUserFrequenciesRepository.delete(id);
            if (hubUserEmailRepository.exists(id)) {
                hubUserEmailRepository.delete(id);
            }
            if (hubUserHipChatRepository.exists(id)) {
                hubUserHipChatRepository.delete(id);
            }
            if (hubUserSlackRepository.exists(id)) {
                hubUserSlackRepository.delete(id);
            }
            final List<HubUserProjectVersionsRelation> hubUserProjectVersions = hubUserProjectVersionsRepository.findByUserConfigId(id);
            for (final HubUserProjectVersionsRelation hubUserProjectVersion : hubUserProjectVersions) {
                final HubUserProjectVersionsRelationPK key = new HubUserProjectVersionsRelationPK();
                key.userConfigId = hubUserProjectVersion.getUserConfigId();
                key.projectName = hubUserProjectVersion.getProjectName();
                key.projectVersionName = hubUserProjectVersion.getProjectVersionName();
                hubUserProjectVersionsRepository.delete(key);
            }
        } else {
            logger.warn("Attempted to delete Hub User config with id {}, but it did not exist.", id);
        }
    }

    private void saveFrequency(final Long id, final String frequency) {
        final HubUserFrequenciesRelation hubUserFrequency = hubUserFrequenciesRepository.findOne(id);
        if (hubUserFrequency != null) {
            final String storedFrequency = hubUserFrequency.getFrequency();
            if (storedFrequency.equals(frequency)) {
                // No need to update the frequency.
                return;
            }
        }
        hubUserFrequenciesRepository.save(new HubUserFrequenciesRelation(id, frequency));
    }

    private <R extends DatabaseChannelRelation> void saveChannelId(final Long id, final String channelId, final JpaRepository<R, Long> repository, final Class<R> clazz) {
        if (doesChannelConfigNeedUpdate(id, channelId, repository)) {
            if (channelId != null) {
                R channelRelation;
                try {
                    channelRelation = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Unable to save Hub user channel config (hubUserId: {}, channelConfigId: {}) because {} could not be instantiated: {}.", id, channelId, clazz.getSimpleName(), e.getLocalizedMessage());
                    return;
                }
                channelRelation.setUserConfigId(id);
                channelRelation.setChannelConfigId(objectTransformer.stringToLong(channelId));
                repository.save(channelRelation);
            } else {
                repository.delete(id);
            }
        }
    }

    private void saveProjectVersions(final Long id, final List<ProjectVersionConfigWrapper> projectVersionWrappers) {
        if (projectVersionWrappers != null && !projectVersionWrappers.isEmpty()) {
            projectVersionWrappers.forEach(projectVersionWrapper -> {
                final HubUserProjectVersionsRelationPK key = new HubUserProjectVersionsRelationPK();
                key.userConfigId = id;
                key.projectName = projectVersionWrapper.getProjectName();
                key.projectVersionName = projectVersionWrapper.getProjectVersionName();
                final Boolean isEnabled = objectTransformer.stringToBoolean(projectVersionWrapper.getEnabled());
                // If the key already exists, no need to update anything
                if (!hubUserProjectVersionsRepository.exists(key)) {
                    hubUserProjectVersionsRepository.save(new HubUserProjectVersionsRelation(key.userConfigId, key.projectName, key.projectVersionName, isEnabled));
                }
            });
        }
    }

    private <R extends DatabaseChannelRelation> boolean doesChannelConfigNeedUpdate(final Long userId, final String channelConfigId, final JpaRepository<R, Long> repository) {
        final Long channelConfigIdLong = objectTransformer.stringToLong(channelConfigId);
        if (channelConfigIdLong != null) {
            return doesChannelConfigNeedUpdate(userId, channelConfigIdLong, repository);
        }
        return repository.exists(userId);
    }

    private <R extends DatabaseChannelRelation> boolean doesChannelConfigNeedUpdate(final Long userId, final Long channelConfigId, final JpaRepository<R, Long> repository) {
        final R relation = repository.findOne(userId);
        if (relation != null) {
            final Long storedChannelConfigId = relation.getChannelConfigId();
            if (storedChannelConfigId.equals(channelConfigId)) {
                return false;
            }
        }
        return true;
    }

    public boolean doesConfigExist(final String id) {
        return doesConfigExist(objectTransformer.stringToLong(id));
    }

    public boolean doesConfigExist(final Long id) {
        if (id != null) {
            return hubUsersRepository.exists(id);
        }
        return false;
    }

    public String getHubUserFrequency(final Long hubUserConfigId) {
        final HubUserFrequenciesRelation frequenciesRelation = hubUserFrequenciesRepository.findOne(hubUserConfigId);
        return frequenciesRelation.getFrequency();
    }

    public String getEmailConfigId(final Long hubUserConfigId) {
        return getChannelConfigIdAsString(hubUserConfigId, hubUserEmailRepository);
    }

    public String getHipChatConfigId(final Long hubUserConfigId) {
        return getChannelConfigIdAsString(hubUserConfigId, hubUserHipChatRepository);
    }

    public String getSlackConfigId(final Long hubUserConfigId) {
        return getChannelConfigIdAsString(hubUserConfigId, hubUserSlackRepository);
    }

    public List<ProjectVersionConfigWrapper> getProjectVersions(final Long hubUserConfigId) {
        final List<HubUserProjectVersionsRelation> projectVersionsRelations = hubUserProjectVersionsRepository.findByUserConfigId(hubUserConfigId);
        final List<ProjectVersionConfigWrapper> wrappers = new ArrayList<>(projectVersionsRelations.size());
        projectVersionsRelations.forEach(relation -> {
            final String isEnabled = objectTransformer.objectToString(relation.getEnabled());
            wrappers.add(new ProjectVersionConfigWrapper(relation.getProjectName(), relation.getProjectVersionName(), isEnabled));
        });
        return wrappers;
    }

    private <R extends DatabaseChannelRelation> String getChannelConfigIdAsString(final Long userId, final JpaRepository<R, Long> repository) {
        final R relation = repository.findOne(userId);
        if (relation != null) {
            return objectTransformer.objectToString(relation.getChannelConfigId());
        }
        return null;
    }

    public ObjectTransformer getObjectTransformer() {
        return objectTransformer;
    }

}
