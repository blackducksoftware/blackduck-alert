/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.ConfiguredProjectEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DistributionProjectRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

@Transactional
@Component
public class ConfiguredProjectsActions<R extends CommonDistributionConfigRestModel> {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredProjectsActions.class);

    private final ConfiguredProjectsRepositoryWrapper configuredProjectsRepository;
    private final DistributionProjectRepositoryWrapper distributionProjectRepository;

    @Autowired
    public ConfiguredProjectsActions(final ConfiguredProjectsRepositoryWrapper configuredProjectsRepository, final DistributionProjectRepositoryWrapper distributionProjectRepository) {
        this.configuredProjectsRepository = configuredProjectsRepository;
        this.distributionProjectRepository = distributionProjectRepository;
    }

    public ConfiguredProjectsRepositoryWrapper getConfiguredProjectsRepository() {
        return configuredProjectsRepository;
    }

    public DistributionProjectRepositoryWrapper getDistributionProjectRepository() {
        return distributionProjectRepository;
    }

    public List<String> getConfiguredProjects(final CommonDistributionConfigEntity commonEntity) {
        final List<DistributionProjectRelation> distributionProjects = distributionProjectRepository.findByCommonDistributionConfigId(commonEntity.getId());
        final List<String> configuredProjects = new ArrayList<>(distributionProjects.size());
        for (final DistributionProjectRelation relation : distributionProjects) {
            final ConfiguredProjectEntity entity = configuredProjectsRepository.findOne(relation.getProjectId());
            configuredProjects.add(entity.getProjectName());
        }
        return configuredProjects;
    }

    public void saveConfiguredProjects(final CommonDistributionConfigEntity commonEntity, final R restModel) {
        if (Boolean.TRUE.equals(commonEntity.getFilterByProject())) {
            final List<String> configuredProjectsFromRestModel = restModel.getConfiguredProjects();
            if (configuredProjectsFromRestModel != null) {
                removeOldDistributionProjectRelations(commonEntity.getId());
                addNewDistributionProjectRelations(commonEntity.getId(), configuredProjectsFromRestModel);
                cleanUpConfiguredProjects();
            } else {
                logger.warn("{}: List of configured projects was null; configured projects will not be updated.", commonEntity.getName());
            }
        }
    }

    public void cleanUpConfiguredProjects() {
        final List<ConfiguredProjectEntity> configuredProjects = configuredProjectsRepository.findAll();
        configuredProjects.forEach(configuredProject -> {
            final List<DistributionProjectRelation> distributionProjects = distributionProjectRepository.findByProjectId(configuredProject.getId());
            if (distributionProjects.isEmpty()) {
                configuredProjectsRepository.delete(configuredProject);
            }
        });
    }

    private void removeOldDistributionProjectRelations(final Long commonDistributionConfigId) {
        final List<DistributionProjectRelation> distributionProjects = distributionProjectRepository.findByCommonDistributionConfigId(commonDistributionConfigId);
        distributionProjectRepository.delete(distributionProjects);
    }

    private void addNewDistributionProjectRelations(final Long commonDistributionConfigId, final List<String> configuredProjectsFromRestModel) {
        for (final String projectName : configuredProjectsFromRestModel) {
            Long projectId;
            final ConfiguredProjectEntity foundEntity = configuredProjectsRepository.findByProjectName(projectName);
            if (foundEntity != null) {
                projectId = foundEntity.getId();
            } else {
                final ConfiguredProjectEntity createdEntity = configuredProjectsRepository.save(new ConfiguredProjectEntity(projectName));
                projectId = createdEntity.getId();
            }
            distributionProjectRepository.save(new DistributionProjectRelation(commonDistributionConfigId, projectId));
        }
    }

}
