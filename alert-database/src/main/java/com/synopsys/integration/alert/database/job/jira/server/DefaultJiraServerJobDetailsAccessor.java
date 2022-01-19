/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldEntity;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldRepository;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class DefaultJiraServerJobDetailsAccessor implements JiraServerJobDetailsAccessor {
    private final JiraServerChannelKey channelKey;
    private final JiraServerJobDetailsRepository jiraServerJobDetailsRepository;
    private final JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository;

    @Autowired
    public DefaultJiraServerJobDetailsAccessor(JiraServerChannelKey channelKey, JiraServerJobDetailsRepository jiraServerJobDetailsRepository,
        JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository) {
        this.channelKey = channelKey;
        this.jiraServerJobDetailsRepository = jiraServerJobDetailsRepository;
        this.jiraServerJobCustomFieldRepository = jiraServerJobCustomFieldRepository;
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return channelKey;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JiraServerJobDetailsModel> retrieveDetails(UUID jobId) {
        return jiraServerJobDetailsRepository.findById(jobId).map(this::convertToModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerJobDetailsModel saveJobDetails(UUID jobId, DistributionJobDetailsModel jobDetailsModel) {
        JiraServerJobDetailsModel jiraJobDetailsModel = jobDetailsModel.getAs(JiraServerJobDetailsModel.class);
        return saveConcreteJobDetails(jobId, jiraJobDetailsModel);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerJobDetailsModel saveConcreteJobDetails(UUID jobId, JiraServerJobDetailsModel jobDetails) {
        JiraServerJobDetailsEntity jiraServerJobDetailsToSave = new JiraServerJobDetailsEntity(
            jobId,
            jobDetails.isAddComments(),
            jobDetails.getIssueCreatorUsername(),
            jobDetails.getProjectNameOrKey(),
            jobDetails.getIssueType(),
            jobDetails.getResolveTransition(),
            jobDetails.getReopenTransition(),
            jobDetails.getIssueSummary()
        );
        JiraServerJobDetailsEntity savedJobDetails = jiraServerJobDetailsRepository.save(jiraServerJobDetailsToSave);

        jiraServerJobCustomFieldRepository.bulkDeleteByJobId(jobId);
        List<JiraServerJobCustomFieldEntity> customFieldsToSave = jobDetails.getCustomFields()
            .stream()
            .map(model -> new JiraServerJobCustomFieldEntity(savedJobDetails.getJobId(), model.getFieldName(), model.getFieldValue()))
            .collect(Collectors.toList());
        List<JiraServerJobCustomFieldEntity> savedJobCustomFields = jiraServerJobCustomFieldRepository.saveAll(customFieldsToSave);
        savedJobDetails.setJobCustomFields(savedJobCustomFields);
        return convertToModel(savedJobDetails);
    }

    private JiraServerJobDetailsModel convertToModel(JiraServerJobDetailsEntity jobDetails) {
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
            customFields,
            jobDetails.getIssueSummary());
    }
}
