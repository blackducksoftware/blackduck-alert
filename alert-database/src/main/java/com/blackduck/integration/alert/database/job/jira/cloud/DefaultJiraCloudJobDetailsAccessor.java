/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.jira.cloud;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.persistence.accessor.JiraCloudJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.blackduck.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldEntity;
import com.blackduck.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldRepository;

@Component
public class DefaultJiraCloudJobDetailsAccessor implements JiraCloudJobDetailsAccessor {
    private final JiraCloudJobDetailsRepository jiraCloudJobDetailsRepository;
    private final JiraCloudJobCustomFieldRepository jiraCloudJobCustomFieldRepository;

    @Autowired
    public DefaultJiraCloudJobDetailsAccessor(JiraCloudJobDetailsRepository jiraCloudJobDetailsRepository, JiraCloudJobCustomFieldRepository jiraCloudJobCustomFieldRepository) {
        this.jiraCloudJobDetailsRepository = jiraCloudJobDetailsRepository;
        this.jiraCloudJobCustomFieldRepository = jiraCloudJobCustomFieldRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JiraCloudJobDetailsModel> retrieveDetails(UUID jobId) {
        return jiraCloudJobDetailsRepository.findById(jobId).map(this::convertToModel);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public JiraCloudJobDetailsEntity saveJiraCloudJobDetails(UUID jobId, JiraCloudJobDetailsModel jiraCloudJobDetails) {
        JiraCloudJobDetailsEntity jiraCloudJobDetailsToSave = new JiraCloudJobDetailsEntity(
            jobId,
            jiraCloudJobDetails.getIssueCreatorEmail(),
            jiraCloudJobDetails.getProjectNameOrKey(),
            jiraCloudJobDetails.getIssueType(),
            jiraCloudJobDetails.getResolveTransition(),
            jiraCloudJobDetails.getReopenTransition(),
            jiraCloudJobDetails.getIssueSummary()
        );
        JiraCloudJobDetailsEntity savedJobDetails = jiraCloudJobDetailsRepository.save(jiraCloudJobDetailsToSave);
        jiraCloudJobCustomFieldRepository.bulkDeleteByJobId(jobId);
        List<JiraCloudJobCustomFieldEntity> customFieldsToSave = jiraCloudJobDetails.getCustomFields()
                                                                     .stream()
                                                                     .map(model -> new JiraCloudJobCustomFieldEntity(savedJobDetails.getJobId(), model.getFieldName(), model.getFieldValue(), model.isTreatValueAsJson()))
                                                                     .toList();
        List<JiraCloudJobCustomFieldEntity> savedCustomFields = jiraCloudJobCustomFieldRepository.saveAll(customFieldsToSave);
        savedJobDetails.setJobCustomFields(savedCustomFields);
        return savedJobDetails;
    }

    public List<JiraJobCustomFieldModel> retrieveCustomFieldsForJob(UUID jobId) {
        return jiraCloudJobCustomFieldRepository.findByJobId(jobId)
                   .stream()
                   .map(customFieldEntity -> new JiraJobCustomFieldModel(customFieldEntity.getFieldName(), customFieldEntity.getFieldValue()))
                   .toList();
    }

    private JiraCloudJobDetailsModel convertToModel(JiraCloudJobDetailsEntity jobDetails) {
        List<JiraJobCustomFieldModel> customFields = jiraCloudJobCustomFieldRepository.findByJobId(jobDetails.getJobId())
                                                         .stream()
                                                         .map(entity -> new JiraJobCustomFieldModel(entity.getFieldName(), entity.getFieldValue(), entity.isTreatValueAsJson()))
                                                         .toList();
        return new JiraCloudJobDetailsModel(
            jobDetails.getJobId(),
            jobDetails.getIssueCreatorEmail(),
            jobDetails.getProjectNameOrKey(),
            jobDetails.getIssueType(),
            jobDetails.getResolveTransition(),
            jobDetails.getReopenTransition(),
            customFields,
            jobDetails.getIssueSummary());
    }

}
