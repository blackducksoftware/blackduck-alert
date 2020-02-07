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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.PolarisIssueAccessor;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.alert.database.provider.polaris.issue.PolarisIssueEntity;
import com.synopsys.integration.alert.database.provider.polaris.issue.PolarisIssueRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;

@Component
@Transactional
public class DefaultPolarisIssueAccessor implements PolarisIssueAccessor {
    private final PolarisIssueRepository polarisIssueRepository;
    private final ProviderProjectRepository providerProjectRepository;

    @Autowired
    public DefaultPolarisIssueAccessor(final PolarisIssueRepository polarisIssueRepository, final ProviderProjectRepository providerProjectRepository) {
        this.polarisIssueRepository = polarisIssueRepository;
        this.providerProjectRepository = providerProjectRepository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<PolarisIssueModel> getProjectIssues(final String projectHref) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(projectHref)) {
            throw new AlertDatabaseConstraintException("The field projectHref cannot be blank");
        }
        final Long projectId = providerProjectRepository.findFirstByHref(projectHref)
                                   .map(ProviderProjectEntity::getId)
                                   .orElseThrow(() -> new AlertDatabaseConstraintException("No project with that href existed: " + projectHref));
        return polarisIssueRepository.findByProjectId(projectId)
                   .stream()
                   .filter(entity -> projectId.equals(entity.getProjectId()))
                   .map(entity -> new PolarisIssueModel(entity.getIssueType(), entity.getPreviousCount(), entity.getCurrentCount()))
                   .collect(Collectors.toList());
    }

    @Override
    public Optional<PolarisIssueModel> getProjectIssueByIssueType(final String projectHref, final String issueType) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(projectHref)) {
            throw new AlertDatabaseConstraintException("The field projectHref cannot be blank");
        }
        if (StringUtils.isBlank(issueType)) {
            throw new AlertDatabaseConstraintException("The field issueType cannot be blank");
        }

        final Optional<Long> optionalProjectId = providerProjectRepository.findFirstByHref(projectHref).map(ProviderProjectEntity::getId);
        if (optionalProjectId.isPresent()) {
            return polarisIssueRepository.findByProjectId(optionalProjectId.get())
                       .stream()
                       .filter(entity -> issueType.equals(entity.getIssueType()))
                       .findFirst()
                       .map(entity -> new PolarisIssueModel(entity.getIssueType(), entity.getPreviousCount(), entity.getCurrentCount()));
        }
        return Optional.empty();
    }

    @Override
    public PolarisIssueModel updateIssueType(final String projectHref, final String issueType, final Integer newCount) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(projectHref)) {
            throw new AlertDatabaseConstraintException("The field projectHref cannot be blank");
        }
        if (StringUtils.isBlank(issueType)) {
            throw new AlertDatabaseConstraintException("The field issueType cannot be blank");
        }
        if (null == newCount) {
            throw new AlertDatabaseConstraintException("The field newCount cannot be null");
        }

        final Long projectId = providerProjectRepository.findFirstByHref(projectHref)
                                   .map(ProviderProjectEntity::getId)
                                   .orElseThrow(() -> new AlertDatabaseConstraintException("No project with that href existed: " + projectHref));
        final Optional<PolarisIssueEntity> optionalIssueEntity = polarisIssueRepository.findFirstByIssueTypeAndProjectId(issueType, projectId);

        final PolarisIssueEntity newIssueEntity;
        if (optionalIssueEntity.isPresent()) {
            final PolarisIssueEntity oldIssueEntity = optionalIssueEntity.get();
            newIssueEntity = new PolarisIssueEntity(oldIssueEntity.getIssueType(), oldIssueEntity.getCurrentCount(), newCount, projectId);
            newIssueEntity.setId(oldIssueEntity.getId());
        } else {
            newIssueEntity = new PolarisIssueEntity(issueType, 0, newCount, projectId);
            polarisIssueRepository.save(newIssueEntity);
        }
        final PolarisIssueEntity savedIssueEntity = polarisIssueRepository.save(newIssueEntity);
        return new PolarisIssueModel(savedIssueEntity.getIssueType(), savedIssueEntity.getPreviousCount(), savedIssueEntity.getCurrentCount());
    }

}
