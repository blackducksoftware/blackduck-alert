/**
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;

@Deprecated(since = "6.4.0")
@Transactional
public class DefaultProviderDataAccessor {
    //implements ProviderDataAccessor {
    public static final int MAX_DESCRIPTION_LENGTH = 250;
    public static final int MAX_PROJECT_NAME_LENGTH = 507;

    private final Logger logger = LoggerFactory.getLogger(DefaultProviderDataAccessor.class);
    private final ProviderProjectRepository providerProjectRepository;
    private final ProviderUserProjectRelationRepository providerUserProjectRelationRepository;
    private final ProviderUserRepository providerUserRepository;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public DefaultProviderDataAccessor(ProviderProjectRepository providerProjectRepository, ProviderUserProjectRelationRepository providerUserProjectRelationRepository, ProviderUserRepository providerUserRepository,
        ConfigurationAccessor configurationAccessor) {
        this.providerProjectRepository = providerProjectRepository;
        this.providerUserProjectRelationRepository = providerUserProjectRelationRepository;
        this.providerUserRepository = providerUserRepository;
        this.configurationAccessor = configurationAccessor;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName) {
        return configurationAccessor.getProviderConfigurationByName(providerConfigName)
                   .map(ConfigurationModel::getConfigurationId)
                   .map(providerProjectRepository::findByProviderConfigId)
                   .stream()
                   .flatMap(List::stream)
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId) {
        return providerProjectRepository.findByProviderConfigId(providerConfigId)
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    public void deleteProjects(Collection<ProviderProject> providerProjects) {
        providerProjects.forEach(project -> providerProjectRepository.deleteByHref(project.getHref()));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String projectHref) {
        Optional<Long> projectId = providerProjectRepository.findFirstByHref(projectHref).map(ProviderProjectEntity::getId);
        if (projectId.isPresent()) {
            Set<Long> userIds = providerUserProjectRelationRepository.findByProviderProjectId(projectId.get())
                                    .stream()
                                    .map(ProviderUserProjectRelation::getProviderUserId)
                                    .collect(Collectors.toSet());
            return providerUserRepository.findAllById(userIds)
                       .stream()
                       .map(ProviderUserEntity::getEmailAddress)
                       .collect(Collectors.toSet());
        }
        return Set.of();
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId) {
        if (null == providerConfigId) {
            return List.of();
        }
        return providerUserRepository.findByProviderConfigId(providerConfigId)
                   .stream()
                   .map(this::convertToUserModel)
                   .collect(Collectors.toList());
    }

    public List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName) {
        if (StringUtils.isBlank(providerConfigName)) {
            return List.of();
        }

        Optional<Long> optionalProviderConfigId = configurationAccessor.getProviderConfigurationByName(providerConfigName)
                                                      .map(ConfigurationModel::getConfigurationId);
        if (optionalProviderConfigId.isPresent()) {
            return getUsersByProviderConfigId(optionalProviderConfigId.get());
        }
        return List.of();
    }

    public void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers) {
        updateProjectDB(providerConfigId, projectToUserData.keySet());
        Set<String> userData = projectToUserData.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        userData.addAll(additionalRelevantUsers);
        updateUserDB(providerConfigId, userData);
        updateUserProjectRelations(providerConfigId, projectToUserData);
    }

    private void mapUsersToProjectByEmail(Long providerConfigId, String projectHref, Collection<String> emailAddresses) throws AlertConfigurationException {
        ProviderProjectEntity project = providerProjectRepository.findFirstByHref(projectHref)
                                            .orElseThrow(() -> new AlertConfigurationException("A project with the following href did not exist: " + projectHref));
        Long projectId = project.getId();
        List<ProviderUserProjectRelation> userRelationsToRemove = providerUserProjectRelationRepository.findByProviderProjectId(projectId);
        List<ProviderUserProjectRelation> userRelationsToAdd = new LinkedList<>();
        List<ProviderUserEntity> providerUserEntities = providerUserRepository.findByEmailAddressInAndProviderConfigId(emailAddresses, providerConfigId);
        for (ProviderUserEntity userEntity : providerUserEntities) {
            Long userId = userEntity.getId();
            userRelationsToAdd.add(new ProviderUserProjectRelation(userId, projectId));
            userRelationsToRemove.removeIf(entity -> entity.getProviderUserId().equals(userId)
                                                         && entity.getProviderProjectId().equals(projectId));
        }
        logger.debug("Adding {} user relations to project {} ", userRelationsToAdd.size(), project.getName());
        logger.debug("Removing {} user relations from project {} ", userRelationsToRemove.size(), project.getName());
        providerUserProjectRelationRepository.saveAll(userRelationsToAdd);
        providerUserProjectRelationRepository.deleteAll(userRelationsToRemove);
        providerUserProjectRelationRepository.flush();
    }

    private List<ProviderUserModel> saveUsers(Long providerConfigId, Collection<ProviderUserModel> users) {
        return users
                   .stream()
                   .map(user -> convertToUserEntity(providerConfigId, user))
                   .map(providerUserRepository::save)
                   .map(this::convertToUserModel)
                   .collect(Collectors.toList());
    }

    private void deleteUsers(Long providerConfigId, Collection<ProviderUserModel> users) {
        users.forEach(user -> providerUserRepository.deleteByProviderConfigIdAndEmailAddress(providerConfigId, user.getEmailAddress()));
    }

    private List<ProviderProject> saveProjects(Long providerConfigId, Collection<ProviderProject> providerProjects) {
        Iterable<ProviderProjectEntity> providerProjectEntities = providerProjects
                                                                      .stream()
                                                                      .map(project -> convertToProjectEntity(providerConfigId, project))
                                                                      .collect(Collectors.toSet());
        List<ProviderProjectEntity> savedEntities = providerProjectRepository.saveAll(providerProjectEntities);
        return savedEntities
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    private ProviderProject convertToProjectModel(ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToProjectEntity(Long providerConfigId, ProviderProject providerProject) {
        String trimmedProjectName = StringUtils.abbreviate(providerProject.getName(), MAX_PROJECT_NAME_LENGTH);
        String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), MAX_DESCRIPTION_LENGTH);
        return new ProviderProjectEntity(trimmedProjectName, trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerConfigId);
    }

    private ProviderUserModel convertToUserModel(ProviderUserEntity providerUserEntity) {
        return new ProviderUserModel(providerUserEntity.getEmailAddress(), providerUserEntity.getOptOut());
    }

    private ProviderUserEntity convertToUserEntity(Long providerConfigId, ProviderUserModel providerUserModel) {
        return new ProviderUserEntity(providerUserModel.getEmailAddress(), providerUserModel.getOptOut(), providerConfigId);
    }

    private List<ProviderProject> updateProjectDB(Long providerConfigId, Set<ProviderProject> currentProjects) {
        Set<ProviderProject> projectsToAdd = new HashSet<>();
        Set<ProviderProject> projectsToRemove = new HashSet<>();
        List<ProviderProject> storedProjects = getProjectsByProviderConfigId(providerConfigId);

        projectsToRemove.addAll(storedProjects);
        projectsToRemove.removeIf(currentProjects::contains);

        projectsToAdd.addAll(currentProjects);
        projectsToAdd.removeIf(storedProjects::contains);

        logger.info("Adding {} projects", projectsToAdd.size());
        logger.info("Removing {} projects", projectsToRemove.size());
        deleteProjects(projectsToRemove);
        return saveProjects(providerConfigId, projectsToAdd);
    }

    private void updateUserDB(Long providerConfigId, Set<String> userEmailAddresses) {
        Set<String> emailsToAdd = new HashSet<>();
        Set<String> emailsToRemove = new HashSet<>();

        List<ProviderUserModel> providerUserEntities = getUsersByProviderConfigId(providerConfigId);
        Set<String> storedEmails = providerUserEntities
                                       .stream()
                                       .map(ProviderUserModel::getEmailAddress)
                                       .collect(Collectors.toSet());

        storedEmails.forEach(storedData -> {
            // If the storedData no longer exists in the current then we need to remove the entry
            // If any of the fields have changed in the currentData, then the storedData will not be in the currentData so we will need to remove the old entry
            if (!userEmailAddresses.contains(storedData)) {
                emailsToRemove.add(storedData);
            }
        });
        userEmailAddresses.forEach(currentData -> {
            // If the currentData is not found in the stored data then we will need to add a new entry
            // If any of the fields have changed in the currentData, then it wont be in the stored data so we will need to add a new entry
            if (!storedEmails.contains(currentData)) {
                emailsToAdd.add(currentData);
            }
        });
        logger.info("Adding {} emails", emailsToAdd.size());
        logger.info("Removing {} emails", emailsToRemove.size());

        List<ProviderUserModel> providerUserEntitiesToRemove = providerUserEntities
                                                                   .stream()
                                                                   .filter(userEntity -> emailsToRemove.contains(userEntity.getEmailAddress()))
                                                                   .collect(Collectors.toList());

        List<ProviderUserModel> providerUserEntitiesToAdd = emailsToAdd
                                                                .stream()
                                                                .map(email -> new ProviderUserModel(email, false))
                                                                .collect(Collectors.toList());
        deleteUsers(providerConfigId, providerUserEntitiesToRemove);
        saveUsers(providerConfigId, providerUserEntitiesToAdd);
    }

    private void updateUserProjectRelations(Long providerConfigId, Map<ProviderProject, Set<String>> projectToEmailAddresses) {
        for (Map.Entry<ProviderProject, Set<String>> projectToEmail : projectToEmailAddresses.entrySet()) {
            try {
                mapUsersToProjectByEmail(providerConfigId, projectToEmail.getKey().getHref(), projectToEmail.getValue());
            } catch (AlertConfigurationException e) {
                logger.error("Problem mapping users to projects", e);
            }
        }
    }

}
