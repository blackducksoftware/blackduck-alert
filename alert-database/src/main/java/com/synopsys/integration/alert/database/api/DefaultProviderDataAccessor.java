/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;

@Component
@Transactional
public class DefaultProviderDataAccessor implements ProviderDataAccessor {
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private final Logger logger = LoggerFactory.getLogger(DefaultProviderDataAccessor.class);
    private final ProviderProjectRepository providerProjectRepository;
    private final ProviderUserProjectRelationRepository providerUserProjectRelationRepository;
    private final ProviderUserRepository providerUserRepository;

    @Autowired
    public DefaultProviderDataAccessor(final ProviderProjectRepository providerProjectRepository, final ProviderUserProjectRelationRepository providerUserProjectRelationRepository, final ProviderUserRepository providerUserRepository) {
        this.providerProjectRepository = providerProjectRepository;
        this.providerUserProjectRelationRepository = providerUserProjectRelationRepository;
        this.providerUserRepository = providerUserRepository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<ProviderProject> findFirstByHref(final String href) {
        return providerProjectRepository.findFirstByHref(href).map(this::convertToProjectModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<ProviderProject> findFirstByName(final String name) {
        return providerProjectRepository.findFirstByName(name).map(this::convertToProjectModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderProject> findByProviderName(final String providerName) {
        return providerProjectRepository.findByProvider(providerName)
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    @Override
    public ProviderProject saveProject(final String providerName, final ProviderProject providerProject) {
        final ProviderProjectEntity trimmedBlackDuckProjectEntity = convertToProjectEntity(providerName, providerProject);
        return convertToProjectModel(providerProjectRepository.save(trimmedBlackDuckProjectEntity));
    }

    @Override
    public void deleteProjects(final String providerName, final Collection<ProviderProject> providerProjects) {
        providerProjects.forEach(project -> providerProjectRepository.deleteByHref(project.getHref()));
    }

    @Override
    public List<ProviderProject> saveProjects(final String providerName, final Collection<ProviderProject> providerProjects) {
        final Iterable<ProviderProjectEntity> providerProjectEntities = providerProjects
                                                                            .stream()
                                                                            .map(project -> convertToProjectEntity(providerName, project))
                                                                            .collect(Collectors.toSet());
        final List<ProviderProjectEntity> savedEntities = providerProjectRepository.saveAll(providerProjectEntities);
        return savedEntities
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    @Override
    public void deleteByHref(final String projectHref) {
        providerProjectRepository.deleteByHref(projectHref);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Set<String> getEmailAddressesForProjectHref(final String projectHref) {
        final Optional<Long> projectId = providerProjectRepository.findFirstByHref(projectHref).map(ProviderProjectEntity::getId);
        if (projectId.isPresent()) {
            final Set<Long> userIds = providerUserProjectRelationRepository.findByProviderProjectId(projectId.get())
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

    @Override
    public void remapUsersToProjectByEmail(final String projectHref, final Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        final ProviderProjectEntity project = providerProjectRepository.findFirstByHref(projectHref)
                                                  .orElseThrow(() -> new AlertDatabaseConstraintException("A project with the following href did not exist: " + projectHref));
        final Long projectId = project.getId();
        providerUserProjectRelationRepository.deleteAllByProviderProjectId(projectId);
        for (final String emailAddress : emailAddresses) {
            providerUserRepository.findByEmailAddressAndProvider(emailAddress, project.getProvider())
                .stream()
                .map(ProviderUserEntity::getId)
                .forEach(userId -> providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(userId, projectId)));
        }
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderUserModel> getAllUsers(final String providerName) {
        return providerUserRepository.findByProvider(providerName)
                   .stream()
                   .map(this::convertToUserModel)
                   .collect(Collectors.toList());
    }

    @Override
    public void deleteUsers(final String providerName, final Collection<ProviderUserModel> users) {
        users.forEach(user -> providerUserRepository.deleteByProviderAndEmailAddress(providerName, user.getEmailAddress()));
    }

    @Override
    public List<ProviderUserModel> saveUsers(final String providerName, final Collection<ProviderUserModel> users) {
        final List<ProviderUserModel> savedUsers = users
                                                       .stream()
                                                       .map(user -> convertToUserEntity(providerName, user))
                                                       .map(providerUserRepository::save)
                                                       .map(entity -> convertToUserModel(entity))
                                                       .collect(Collectors.toList());
        return savedUsers;
    }

    @Override
    public void updateProjectAndUserData(final String providerName, final Map<ProviderProject, Set<String>> projectToUserData) {
        updateProjectDB(providerName, projectToUserData.keySet());
        final Set<String> userData = projectToUserData.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        updateUserDB(providerName, userData);
        updateUserProjectRelations(projectToUserData);
    }

    private ProviderProject convertToProjectModel(final ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToProjectEntity(final String providerName, final ProviderProject providerProject) {
        final String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), MAX_DESCRIPTION_LENGTH);
        return new ProviderProjectEntity(providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerName);
    }

    private ProviderUserModel convertToUserModel(final ProviderUserEntity providerUserEntity) {
        return new ProviderUserModel(providerUserEntity.getEmailAddress(), providerUserEntity.getOptOut());
    }

    private ProviderUserEntity convertToUserEntity(final String providerName, final ProviderUserModel providerUserModel) {
        return new ProviderUserEntity(providerUserModel.getEmailAddress(), providerUserModel.getOptOut(), providerName);
    }

    private List<ProviderProject> updateProjectDB(final String providerName, final Set<ProviderProject> currentProjects) {
        final Set<ProviderProject> projectsToAdd = new HashSet<>();
        final Set<ProviderProject> projectsToRemove = new HashSet<>();
        final List<ProviderProject> storedProjects = findByProviderName(providerName);

        projectsToRemove.addAll(storedProjects);
        projectsToRemove.removeIf(project -> currentProjects.contains(project));

        projectsToAdd.addAll(currentProjects);
        projectsToAdd.removeIf(project -> storedProjects.contains(project));

        logger.info("Adding {} projects", projectsToAdd.size());
        logger.info("Removing {} projects", projectsToRemove.size());
        deleteProjects(providerName, projectsToRemove);
        return saveProjects(providerName, projectsToAdd);
    }

    private void updateUserDB(final String providerName, final Set<String> userEmailAddresses) {
        final Set<String> emailsToAdd = new HashSet<>();
        final Set<String> emailsToRemove = new HashSet<>();

        final List<ProviderUserModel> providerUserEntities = getAllUsers(providerName);
        final Set<String> storedEmails = providerUserEntities
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

        final List<ProviderUserModel> providerUserEntitiesToRemove = providerUserEntities
                                                                         .stream()
                                                                         .filter(userEntity -> emailsToRemove.contains(userEntity.getEmailAddress()))
                                                                         .collect(Collectors.toList());

        final List<ProviderUserModel> providerUserEntityList = emailsToAdd
                                                                   .stream()
                                                                   .map(email -> new ProviderUserModel(email, false))
                                                                   .collect(Collectors.toList());
        deleteUsers(providerName, providerUserEntitiesToRemove);
        saveUsers(providerName, providerUserEntityList);
    }

    private void updateUserProjectRelations(final Map<ProviderProject, Set<String>> projectToEmailAddresses) {
        final Set<ProviderUserProjectRelation> userProjectRelations = new HashSet<>();
        for (final Map.Entry<ProviderProject, Set<String>> projectToEmail : projectToEmailAddresses.entrySet()) {
            try {
                remapUsersToProjectByEmail(projectToEmail.getKey().getHref(), projectToEmail.getValue());
            } catch (final AlertDatabaseConstraintException e) {
                logger.error("Problem mapping users to projects", e);
            }
        }
        logger.info("User to project relationships {}", userProjectRelations.size());
    }

}
