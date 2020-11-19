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
package com.synopsys.integration.alert.database.job.blackduck;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeEntity;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeRepository;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterRepository;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectEntity;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectRepository;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterRepository;

@Component
public class BlackDuckJobDetailsAccessor {
    private final BlackDuckJobDetailsRepository blackDuckJobDetailsRepository;
    private final BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository;
    private final BlackDuckJobProjectRepository blackDuckJobProjectRepository;
    private final BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository;
    private final BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository;

    @Autowired
    public BlackDuckJobDetailsAccessor(
        BlackDuckJobDetailsRepository blackDuckJobDetailsRepository,
        BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository,
        BlackDuckJobProjectRepository blackDuckJobProjectRepository,
        BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository,
        BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository
    ) {
        this.blackDuckJobDetailsRepository = blackDuckJobDetailsRepository;
        this.blackDuckJobNotificationTypeRepository = blackDuckJobNotificationTypeRepository;
        this.blackDuckJobProjectRepository = blackDuckJobProjectRepository;
        this.blackDuckJobPolicyFilterRepository = blackDuckJobPolicyFilterRepository;
        this.blackDuckJobVulnerabilitySeverityFilterRepository = blackDuckJobVulnerabilitySeverityFilterRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public BlackDuckJobDetailsEntity saveBlackDuckJobDetails(UUID jobId, DistributionJobModel distributionJobModel) {
        BlackDuckJobDetailsEntity blackDuckJobDetailsToSave = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobModel.getBlackDuckGlobalConfigId(),
            distributionJobModel.isFilterByProject(),
            distributionJobModel.getProjectNamePattern().orElse(null)
        );
        BlackDuckJobDetailsEntity savedBlackDuckJobDetails = blackDuckJobDetailsRepository.save(blackDuckJobDetailsToSave);

        List<BlackDuckJobNotificationTypeEntity> notificationTypesToSave = distributionJobModel.getNotificationTypes()
                                                                               .stream()
                                                                               .map(notificationType -> new BlackDuckJobNotificationTypeEntity(jobId, notificationType))
                                                                               .collect(Collectors.toList());
        List<BlackDuckJobNotificationTypeEntity> savedNotificationTypes = blackDuckJobNotificationTypeRepository.saveAll(notificationTypesToSave);
        savedBlackDuckJobDetails.setBlackDuckJobNotificationTypes(savedNotificationTypes);

        List<BlackDuckJobProjectEntity> ProjectFiltersToSave = distributionJobModel.getProjectFilterDetails()
                                                                   .stream()
                                                                   .map(projectDetails -> new BlackDuckJobProjectEntity(jobId, projectDetails.getName(), projectDetails.getHref(), projectDetails.getProjectOwnerEmail().orElse(null)))
                                                                   .collect(Collectors.toList());
        List<BlackDuckJobProjectEntity> savedProjectFilters = blackDuckJobProjectRepository.saveAll(ProjectFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobProjects(savedProjectFilters);

        List<BlackDuckJobPolicyFilterEntity> policyFiltersToSave = distributionJobModel.getPolicyFilterPolicyNames()
                                                                       .stream()
                                                                       .map(policyName -> new BlackDuckJobPolicyFilterEntity(jobId, policyName))
                                                                       .collect(Collectors.toList());
        List<BlackDuckJobPolicyFilterEntity> savedPolicyFilters = blackDuckJobPolicyFilterRepository.saveAll(policyFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobPolicyFilters(savedPolicyFilters);

        List<BlackDuckJobVulnerabilitySeverityFilterEntity> vulnerabilitySeverityFiltersToSave = distributionJobModel.getVulnerabilityFilterSeverityNames()
                                                                                                     .stream()
                                                                                                     .map(severityName -> new BlackDuckJobVulnerabilitySeverityFilterEntity(jobId, severityName))
                                                                                                     .collect(Collectors.toList());
        List<BlackDuckJobVulnerabilitySeverityFilterEntity> savedVulnerabilitySeverityFilters = blackDuckJobVulnerabilitySeverityFilterRepository.saveAll(vulnerabilitySeverityFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobVulnerabilitySeverityFilters(savedVulnerabilitySeverityFilters);

        return savedBlackDuckJobDetails;
    }

    public List<String> retrieveNotificationTypesForJob(UUID jobId) {
        return blackDuckJobNotificationTypeRepository.findByJobId(jobId)
                   .stream()
                   .map(BlackDuckJobNotificationTypeEntity::getNotificationType)
                   .collect(Collectors.toList());
    }

    public List<String> retrieveProjectNamesForJob(UUID jobId) {
        return blackDuckJobProjectRepository.findByJobId(jobId)
                   .stream()
                   .map(BlackDuckJobProjectEntity::getProjectName)
                   .collect(Collectors.toList());
    }

    public List<String> retrievePolicyNamesForJob(UUID jobId) {
        return blackDuckJobPolicyFilterRepository.findByJobId(jobId)
                   .stream()
                   .map(BlackDuckJobPolicyFilterEntity::getPolicyName)
                   .collect(Collectors.toList());
    }

    public List<String> retrieveVulnerabilitySeverityNamesForJob(UUID jobId) {
        return blackDuckJobVulnerabilitySeverityFilterRepository.findByJobId(jobId)
                   .stream()
                   .map(BlackDuckJobVulnerabilitySeverityFilterEntity::getSeverityName)
                   .collect(Collectors.toList());
    }

}
