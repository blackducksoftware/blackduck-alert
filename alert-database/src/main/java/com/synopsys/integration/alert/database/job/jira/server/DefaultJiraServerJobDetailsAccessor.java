/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.jira.server;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.JiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldEntity;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldRepository;

@Component
public class DefaultJiraServerJobDetailsAccessor implements JiraServerJobDetailsAccessor {
    private final JiraServerJobDetailsRepository jiraServerJobDetailsRepository;
    private final JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository;

    @Autowired
    public DefaultJiraServerJobDetailsAccessor(JiraServerJobDetailsRepository jiraServerJobDetailsRepository,
        JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository) {
        this.jiraServerJobDetailsRepository = jiraServerJobDetailsRepository;
        this.jiraServerJobCustomFieldRepository = jiraServerJobCustomFieldRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JiraServerJobDetailsModel> retrieveDetails(UUID jobId) {
        return jiraServerJobDetailsRepository.findById(jobId).map(this::covertToModel);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerJobDetailsEntity saveJiraServerJobDetails(UUID jobId, JiraServerJobDetailsModel jiraServerJobDetails) {
        JiraServerJobDetailsEntity jiraServerJobDetailsToSave = new JiraServerJobDetailsEntity(
            jobId,
            jiraServerJobDetails.isAddComments(),
            jiraServerJobDetails.getIssueCreatorUsername(),
            jiraServerJobDetails.getProjectNameOrKey(),
            jiraServerJobDetails.getIssueType(),
            jiraServerJobDetails.getResolveTransition(),
            jiraServerJobDetails.getReopenTransition()
        );
        JiraServerJobDetailsEntity savedJobDetails = jiraServerJobDetailsRepository.save(jiraServerJobDetailsToSave);

        jiraServerJobCustomFieldRepository.deleteByJobId(jobId);
        List<JiraServerJobCustomFieldEntity> customFieldsToSave = jiraServerJobDetails.getCustomFields()
                                                                      .stream()
                                                                      .map(model -> new JiraServerJobCustomFieldEntity(savedJobDetails.getJobId(), model.getFieldName(), model.getFieldValue()))
                                                                      .collect(Collectors.toList());
        List<JiraServerJobCustomFieldEntity> savedJobCustomFields = jiraServerJobCustomFieldRepository.saveAll(customFieldsToSave);
        savedJobDetails.setJobCustomFields(savedJobCustomFields);
        return savedJobDetails;
    }

    public List<JiraJobCustomFieldModel> retrieveCustomFieldsForJob(UUID jobId) {
        return jiraServerJobCustomFieldRepository.findByJobId(jobId)
                   .stream()
                   .map(customFieldEntity -> new JiraJobCustomFieldModel(customFieldEntity.getFieldName(), customFieldEntity.getFieldValue()))
                   .collect(Collectors.toList());
    }

    private JiraServerJobDetailsModel covertToModel(JiraServerJobDetailsEntity jobDetails) {
        List<JiraJobCustomFieldModel> customFields = jiraServerJobCustomFieldRepository.findByJobId(jobDetails.getJobId())
                                                         .stream()
                                                         .map(entity -> new JiraJobCustomFieldModel(entity.getFieldName(), entity.getFieldValue()))
                                                         .collect(Collectors.toList());
        return new JiraServerJobDetailsModel(
            jobDetails.getJobId(),
            jobDetails.getAddComments(),
            jobDetails.getIssueCreatorUsername(),
            jobDetails.getProjectNameOrKey(),
            jobDetails.getIssueType(),
            jobDetails.getResolveTransition(),
            jobDetails.getReopenTransition(),
            customFields
        );
    }
}
