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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;

@Component
@Transactional
public class DefaultProviderDataAccessor implements ProviderDataAccessor {
    public static final Integer DEFAULT_OFFSET = 0;
    public static final Integer DEFAULT_LIMIT = 100;

    private static final int MAX_DESCRIPTION_LENGTH = 250;

    private final Logger logger = LoggerFactory.getLogger(DefaultProviderDataAccessor.class);
    private final ProviderProjectRepository providerProjectRepository;
    private final ProviderUserProjectRelationRepository providerUserProjectRelationRepository;
    private final ProviderUserRepository providerUserRepository;

    @Autowired
    public DefaultProviderDataAccessor(ProviderProjectRepository providerProjectRepository, ProviderUserProjectRelationRepository providerUserProjectRelationRepository, ProviderUserRepository providerUserRepository) {
        this.providerProjectRepository = providerProjectRepository;
        this.providerUserProjectRelationRepository = providerUserProjectRelationRepository;
        this.providerUserRepository = providerUserRepository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<ProviderProject> findFirstByHref(String href) {
        return providerProjectRepository.findFirstByHref(href).map(this::convertToProjectModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<ProviderProject> findFirstByName(String name) {
        return providerProjectRepository.findFirstByName(name).map(this::convertToProjectModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderProject> findByProviderName(String providerName) {
        return providerProjectRepository.findByProvider(providerName)
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderProject> findByProviderKey(DescriptorKey descriptorKey) {
        return providerProjectRepository.findByProvider(descriptorKey.getUniversalKey())
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    @Override
    public ProviderProject saveProject(DescriptorKey descriptorKey, ProviderProject providerProject) {
        ProviderProjectEntity trimmedBlackDuckProjectEntity = convertToProjectEntity(descriptorKey, providerProject);
        return convertToProjectModel(providerProjectRepository.save(trimmedBlackDuckProjectEntity));
    }

    @Override
    public void deleteProjects(DescriptorKey descriptorKey, Collection<ProviderProject> providerProjects) {
        providerProjects.forEach(project -> providerProjectRepository.deleteByHref(project.getHref()));
    }

    @Override
    public List<ProviderProject> saveProjects(DescriptorKey descriptorKey, Collection<ProviderProject> providerProjects) {
        Iterable<ProviderProjectEntity> providerProjectEntities = providerProjects
                                                                      .stream()
                                                                      .map(project -> convertToProjectEntity(descriptorKey, project))
                                                                      .collect(Collectors.toSet());
        List<ProviderProjectEntity> savedEntities = providerProjectRepository.saveAll(providerProjectEntities);
        return savedEntities
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    @Override
    public void deleteByHref(String projectHref) {
        providerProjectRepository.deleteByHref(projectHref);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Set<String> getEmailAddressesForProjectHref(String projectHref) {
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

    @Override
    public void mapUsersToProjectByEmail(String projectHref, Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        ProviderProjectEntity project = providerProjectRepository.findFirstByHref(projectHref)
                                            .orElseThrow(() -> new AlertDatabaseConstraintException("A project with the following href did not exist: " + projectHref));
        Long projectId = project.getId();
        for (String emailAddress : emailAddresses) {
            providerUserRepository.findByEmailAddressAndProvider(emailAddress, project.getProvider())
                .stream()
                .map(ProviderUserEntity::getId)
                .forEach(userId -> providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(userId, projectId)));
        }
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<ProviderUserModel> getAllUsers(String providerName) {
        return providerUserRepository.findByProvider(providerName)
                   .stream()
                   .map(this::convertToUserModel)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AlertPagedModel<ProviderUserModel> getPageOfUsers(String providerName, Integer pageNumber, Integer pageSize, String q) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(providerName)) {
            throw new AlertDatabaseConstraintException("The field 'providerName' cannot be blank.");
        }
        if (StringUtils.isBlank(q)) {
            q = "@";
        }

        Integer pageNumberToUse = isUsableParam(pageNumber) ? pageNumber : DEFAULT_OFFSET;
        Integer pageSizeToUse = isUsableParam(pageSize) ? pageSize : DEFAULT_LIMIT;
        PageRequest pageRequest = PageRequest.of(pageNumberToUse, pageSizeToUse, new Sort(Sort.Direction.DESC, "emailAddress"));

        Page<ProviderUserEntity> pageOfUsers = providerUserRepository.findPageOfUsersByProviderAndEmailSearchTerm(providerName, q, pageRequest);
        List<ProviderUserModel> userModels = pageOfUsers.getContent()
                                                 .stream()
                                                 .map(entry -> new ProviderUserModel(entry.getEmailAddress(), entry.getOptOut()))
                                                 .collect(Collectors.toList());
        return new AlertPagedModel<>(pageOfUsers.getTotalPages(), pageOfUsers.getNumber(), userModels.size(), userModels);
    }

    @Override
    public void deleteUsers(DescriptorKey descriptorKey, Collection<ProviderUserModel> users) {
        users.forEach(user -> providerUserRepository.deleteByProviderAndEmailAddress(descriptorKey.getUniversalKey(), user.getEmailAddress()));
    }

    @Override
    public List<ProviderUserModel> saveUsers(DescriptorKey descriptorKey, Collection<ProviderUserModel> users) {
        List<ProviderUserModel> savedUsers = users
                                                 .stream()
                                                 .map(user -> convertToUserEntity(descriptorKey, user))
                                                 .map(providerUserRepository::save)
                                                 .map(entity -> convertToUserModel(entity))
                                                 .collect(Collectors.toList());
        return savedUsers;
    }

    @Override
    public void updateProjectAndUserData(DescriptorKey descriptorKey, Map<ProviderProject, Set<String>> projectToUserData) {
        updateProjectDB(descriptorKey, projectToUserData.keySet());
        Set<String> userData = projectToUserData.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        updateUserDB(descriptorKey, userData);
        updateUserProjectRelations(projectToUserData);
    }

    private ProviderProject convertToProjectModel(ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToProjectEntity(DescriptorKey descriptorKey, ProviderProject providerProject) {
        String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), MAX_DESCRIPTION_LENGTH);
        return new ProviderProjectEntity(providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), descriptorKey.getUniversalKey());
    }

    private ProviderUserModel convertToUserModel(ProviderUserEntity providerUserEntity) {
        return new ProviderUserModel(providerUserEntity.getEmailAddress(), providerUserEntity.getOptOut());
    }

    private ProviderUserEntity convertToUserEntity(DescriptorKey descriptorKey, ProviderUserModel providerUserModel) {
        return new ProviderUserEntity(providerUserModel.getEmailAddress(), providerUserModel.getOptOut(), descriptorKey.getUniversalKey());
    }

    private List<ProviderProject> updateProjectDB(DescriptorKey descriptorKey, Set<ProviderProject> currentProjects) {
        Set<ProviderProject> projectsToAdd = new HashSet<>();
        Set<ProviderProject> projectsToRemove = new HashSet<>();
        List<ProviderProject> storedProjects = findByProviderKey(descriptorKey);

        projectsToRemove.addAll(storedProjects);
        projectsToRemove.removeIf(project -> currentProjects.contains(project));

        projectsToAdd.addAll(currentProjects);
        projectsToAdd.removeIf(project -> storedProjects.contains(project));

        logger.info("Adding {} projects", projectsToAdd.size());
        logger.info("Removing {} projects", projectsToRemove.size());
        deleteProjects(descriptorKey, projectsToRemove);
        return saveProjects(descriptorKey, projectsToAdd);
    }

    private void updateUserDB(DescriptorKey descriptorKey, Set<String> userEmailAddresses) {
        Set<String> emailsToAdd = new HashSet<>();
        Set<String> emailsToRemove = new HashSet<>();

        List<ProviderUserModel> providerUserEntities = getAllUsers(descriptorKey.getUniversalKey());
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

        List<ProviderUserModel> providerUserEntityList = emailsToAdd
                                                             .stream()
                                                             .map(email -> new ProviderUserModel(email, false))
                                                             .collect(Collectors.toList());
        deleteUsers(descriptorKey, providerUserEntitiesToRemove);
        saveUsers(descriptorKey, providerUserEntityList);
    }

    private void updateUserProjectRelations(Map<ProviderProject, Set<String>> projectToEmailAddresses) {
        Set<ProviderUserProjectRelation> userProjectRelations = new HashSet<>();
        for (Map.Entry<ProviderProject, Set<String>> projectToEmail : projectToEmailAddresses.entrySet()) {
            try {
                mapUsersToProjectByEmail(projectToEmail.getKey().getHref(), projectToEmail.getValue());
            } catch (AlertDatabaseConstraintException e) {
                logger.error("Problem mapping users to projects", e);
            }
        }
        logger.info("User to project relationships {}", userProjectRelations.size());
    }

    private boolean isUsableParam(Integer param) {
        return null != param && 0 <= param;
    }

}
