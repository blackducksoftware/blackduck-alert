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
import com.synopsys.integration.alert.database.RepositoryAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;

@Component
@Transactional
// TODO update tests for this class
public class ProviderDataAccessor extends RepositoryAccessor<ProviderProjectEntity> {
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private final ProviderProjectRepository providerProjectRepository;
    private final ProviderUserProjectRelationRepository providerUserProjectRelationRepository;
    private final ProviderUserRepository providerUserRepository;

    @Autowired
    public ProviderDataAccessor(final ProviderProjectRepository providerProjectRepository,
        final ProviderUserProjectRelationRepository providerUserProjectRelationRepository, final ProviderUserRepository providerUserRepository) {
        super(providerProjectRepository);
        this.providerProjectRepository = providerProjectRepository;
        this.providerUserProjectRelationRepository = providerUserProjectRelationRepository;
        this.providerUserRepository = providerUserRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<com.synopsys.integration.alert.common.persistence.model.ProviderProject> findByName(final String name) {
        return providerProjectRepository.findFirstByName(name).map(this::convertToModel);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<com.synopsys.integration.alert.common.persistence.model.ProviderProject> findByProviderName(final String providerName) {
        return providerProjectRepository.findByProvider(providerName)
                   .stream()
                   .map(this::convertToModel)
                   .collect(Collectors.toList());
    }

    public com.synopsys.integration.alert.common.persistence.model.ProviderProject saveProject(final String providerName, final com.synopsys.integration.alert.common.persistence.model.ProviderProject providerProject) {
        final String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), MAX_DESCRIPTION_LENGTH);
        final ProviderProjectEntity trimmedBlackDuckProjectEntity = new ProviderProjectEntity(
            providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerName);
        return convertToModel(super.saveEntity(trimmedBlackDuckProjectEntity));
    }

    public List<com.synopsys.integration.alert.common.persistence.model.ProviderProject> deleteAndSaveAll(final String providerName, final Collection<com.synopsys.integration.alert.common.persistence.model.ProviderProject> providerProjects) {
        providerProjectRepository.deleteAllByProvider(providerName);
        final Iterable<ProviderProjectEntity> providerProjectEntities = providerProjects
                                                                            .stream()
                                                                            .map(project -> convertToEntity(providerName, project))
                                                                            .collect(Collectors.toSet());
        final List<ProviderProjectEntity> savedEntities = providerProjectRepository.saveAll(providerProjectEntities);
        return savedEntities
                   .stream()
                   .map(this::convertToModel)
                   .collect(Collectors.toList());
    }

    public void deleteByHref(final String href) {
        providerProjectRepository.deleteByHref(href);
    }

    public Set<String> getEmailAddressesForProjectHref(final String href) {
        final Optional<Long> projectId = providerProjectRepository.findFirstByHref(href).map(ProviderProjectEntity::getId);
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

    public void createProjectToEmailRelation(final String href, final Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        final Long projectId = providerProjectRepository.findFirstByHref(href)
                                   .map(ProviderProjectEntity::getId)
                                   .orElseThrow(() -> new AlertDatabaseConstraintException("A project with the following href did not exist: " + href));
        providerUserProjectRelationRepository.deleteAllByProviderProjectId(projectId);
        for (final String emailAddress : emailAddresses) {
            providerUserRepository.findByEmailAddress(emailAddress)
                .stream()
                .map(ProviderUserEntity::getId)
                .forEach(userId -> providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(userId, projectId)));
        }
    }

    private com.synopsys.integration.alert.common.persistence.model.ProviderProject convertToModel(final ProviderProjectEntity providerProjectEntity) {
        return new com.synopsys.integration.alert.common.persistence.model.ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(), providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToEntity(final String providerName, final com.synopsys.integration.alert.common.persistence.model.ProviderProject providerProject) {
        return new ProviderProjectEntity(providerProject.getName(), providerProject.getDescription(), providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerName);
    }

}
