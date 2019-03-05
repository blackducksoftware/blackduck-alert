/**
 * alert-database
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
        final String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), MAX_DESCRIPTION_LENGTH);
        final ProviderProjectEntity trimmedBlackDuckProjectEntity = new ProviderProjectEntity(
            providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerName);
        return convertToProjectModel(providerProjectRepository.save(trimmedBlackDuckProjectEntity));
    }

    @Override
    public List<ProviderProject> deleteAndSaveAllProjects(final String providerName, final Collection<ProviderProject> providerProjects) {
        providerProjectRepository.deleteAllByProvider(providerName);
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
    public List<ProviderUserModel> deleteAndSaveAllUsers(final String providerName, final Collection<ProviderUserModel> usersToDelete, final Collection<ProviderUserModel> usersToAdd) {
        usersToDelete.forEach(user -> providerUserRepository.deleteByProviderAndEmailAddress(providerName, user.getEmailAddress()));
        usersToAdd
            .stream()
            .map(user -> convertToUserEntity(providerName, user))
            .forEach(providerUserRepository::save);
        return List.copyOf(usersToAdd);
    }

    private ProviderProject convertToProjectModel(final ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToProjectEntity(final String providerName, final ProviderProject providerProject) {
        return new ProviderProjectEntity(providerProject.getName(), providerProject.getDescription(), providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerName);
    }

    private ProviderUserModel convertToUserModel(final ProviderUserEntity providerUserEntity) {
        return new ProviderUserModel(providerUserEntity.getEmailAddress(), providerUserEntity.getOptOut());
    }

    private ProviderUserEntity convertToUserEntity(final String providerName, final ProviderUserModel providerUserModel) {
        return new ProviderUserEntity(providerUserModel.getEmailAddress(), providerUserModel.getOptOut(), providerName);
    }

}
