/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.database.accessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsEntity;
import com.blackduck.integration.alert.channel.jira.server.database.job.JiraServerJobDetailsRepository;
import com.blackduck.integration.alert.channel.jira.server.database.job.custom_field.JiraServerJobCustomFieldEntity;
import com.blackduck.integration.alert.channel.jira.server.database.job.custom_field.JiraServerJobCustomFieldRepository;
import com.blackduck.integration.alert.common.persistence.accessor.JiraServerJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

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
            .map(model -> new JiraServerJobCustomFieldEntity(savedJobDetails.getJobId(), model.getFieldName(), model.getFieldValue(), model.isTreatValueAsJson()))
            .toList();
        List<JiraServerJobCustomFieldEntity> savedJobCustomFields = jiraServerJobCustomFieldRepository.saveAll(customFieldsToSave);
        savedJobDetails.setJobCustomFields(savedJobCustomFields);
        return convertToModel(savedJobDetails);
    }

    private JiraServerJobDetailsModel convertToModel(JiraServerJobDetailsEntity jobDetails) {
        List<JiraJobCustomFieldModel> customFields = jiraServerJobCustomFieldRepository.findByJobId(jobDetails.getJobId())
            .stream()
            .map(entity -> new JiraJobCustomFieldModel(entity.getFieldName(), entity.getFieldValue(), entity.isTreatValueAsJson()))
            .toList();
        return new JiraServerJobDetailsModel(
            jobDetails.getJobId(),
            jobDetails.getIssueCreatorUsername(),
            jobDetails.getProjectNameOrKey(),
            jobDetails.getIssueType(),
            jobDetails.getResolveTransition(),
            jobDetails.getReopenTransition(),
            customFields,
            jobDetails.getIssueSummary());
    }
}
