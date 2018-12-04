/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.entity.ConfiguredProjectEntity;
import com.synopsys.integration.alert.database.entity.repository.ConfiguredProjectsRepository;
import com.synopsys.integration.alert.database.relation.DistributionProjectRelation;
import com.synopsys.integration.alert.database.relation.repository.DistributionProjectRepository;

@Component
public class ConfiguredProjectsActions {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredProjectsActions.class);

    private final ConfiguredProjectsRepository configuredProjectsRepository;
    private final DistributionProjectRepository distributionProjectRepository;

    @Autowired
    public ConfiguredProjectsActions(final ConfiguredProjectsRepository configuredProjectsRepository, final DistributionProjectRepository distributionProjectRepository) {
        this.configuredProjectsRepository = configuredProjectsRepository;
        this.distributionProjectRepository = distributionProjectRepository;
    }

    public ConfiguredProjectsRepository getConfiguredProjectsRepository() {
        return configuredProjectsRepository;
    }

    public DistributionProjectRepository getDistributionProjectRepository() {
        return distributionProjectRepository;
    }

    @Transactional
    public List<String> getConfiguredProjects(final CommonDistributionConfigEntity commonEntity) {
        final List<DistributionProjectRelation> distributionProjects = distributionProjectRepository.findByCommonDistributionConfigId(commonEntity.getId());
        final List<String> configuredProjects = new ArrayList<>(distributionProjects.size());
        for (final DistributionProjectRelation relation : distributionProjects) {
            final Optional<ConfiguredProjectEntity> entity = configuredProjectsRepository.findById(relation.getProjectId());
            entity.ifPresent(presentEntity -> configuredProjects.add(presentEntity.getProjectName()));
        }
        return configuredProjects;
    }

    @Transactional
    // TODO investigate the impact of private methods on transactions; the transaction is not complete until after all of the private methods have executed
    public void saveConfiguredProjects(final long entityId, final List<String> configuredProjects) {
        if (configuredProjects != null) {
            removeOldDistributionProjectRelations(entityId);
            addNewDistributionProjectRelations(entityId, configuredProjects);
            cleanUpConfiguredProjects();
        } else {
            logger.warn("Configured projects was null; configured projects will not be updated.");
        }
    }

    @Transactional
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
        distributionProjectRepository.deleteAll(distributionProjects);
    }

    private void addNewDistributionProjectRelations(final Long commonDistributionConfigId, final List<String> configuredProjectsFromRestModel) {
        for (final String projectName : configuredProjectsFromRestModel) {
            final Long projectId;
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
