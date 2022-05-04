/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.job.JobToNotificationRelation;
import com.synopsys.integration.alert.database.job.JobToNotificationRelationRepository;

@Component
public class DefaultJobNotificationMappingAccessor implements JobNotificationMappingAccessor {
    private JobToNotificationRelationRepository jobToNotificationRelationRepository;

    @Autowired
    public DefaultJobNotificationMappingAccessor(JobToNotificationRelationRepository jobToNotificationRelationRepository) {
        this.jobToNotificationRelationRepository = jobToNotificationRelationRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public AlertPagedModel<JobToNotificationMappingModel> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<JobToNotificationRelation> pageOfData = jobToNotificationRelationRepository.findAllByCorrelationIdAndJobId(correlationId, jobId, pageRequest);
        List<JobToNotificationMappingModel> models = pageOfData.get()
            .map(this::convertToModel)
            .collect(Collectors.toList());
        return new AlertPagedModel<>(
            pageOfData.getTotalPages(),
            pageOfData.getNumber(),
            pageOfData.getSize(),
            models
        );
    }

    @Override
    @Transactional
    public void addJobMapping(UUID correlationId, UUID jobId, Long notificationId) {
        JobToNotificationRelation relation = new JobToNotificationRelation(correlationId, jobId, notificationId);
        jobToNotificationRelationRepository.save(relation);
    }

    @Override
    @Transactional
    public void addJobMappings(List<JobToNotificationMappingModel> jobMappings) {
        List<JobToNotificationRelation> entities = jobMappings.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());
        jobToNotificationRelationRepository.saveAllAndFlush(entities);
    }

    @Override
    @Transactional
    public void removeJobMapping(UUID correlationId, UUID jobId) {
        jobToNotificationRelationRepository.deleteAllByCorrelationIdAndJobId(correlationId, jobId);
    }

    private JobToNotificationRelation convertToEntity(JobToNotificationMappingModel model) {
        return new JobToNotificationRelation(model.getCorrelationId(), model.getJobId(), model.getNotificationId());
    }

    private JobToNotificationMappingModel convertToModel(JobToNotificationRelation relation) {
        return new JobToNotificationMappingModel(relation.getCorrelationId(), relation.getJobId(), relation.getNotificationId());
    }
}
