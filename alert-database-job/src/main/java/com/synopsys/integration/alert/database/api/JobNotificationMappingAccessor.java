/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.job.JobToNotificationRelation;
import com.synopsys.integration.alert.database.job.JobToNotificationRelationRepository;

@Component
public class JobNotificationMappingAccessor {
    private JobToNotificationRelationRepository jobToNotificationRelationRepository;

    @Autowired
    public JobNotificationMappingAccessor(JobToNotificationRelationRepository jobToNotificationRelationRepository) {
        this.jobToNotificationRelationRepository = jobToNotificationRelationRepository;
    }

    public AlertPagedModel<JobToNotificationRelation> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<JobToNotificationRelation> jobToNotificationMappings = jobToNotificationRelationRepository.findAllByCorrelationIdAndJobId(correlationId, jobId, pageRequest);
        return new AlertPagedModel<>(
            jobToNotificationMappings.getTotalPages(),
            jobToNotificationMappings.getNumber(),
            jobToNotificationMappings.getSize(),
            jobToNotificationMappings.getContent()
        );
    }

    @Transactional
    public void addJobMapping(UUID correlationId, UUID jobId, Long notificationId) {
        JobToNotificationRelation relation = new JobToNotificationRelation(correlationId, jobId, notificationId);
        jobToNotificationRelationRepository.save(relation);
    }

    @Transactional
    public void removeJobMapping(UUID correlationId, UUID jobId) {
        jobToNotificationRelationRepository.deleteAllByCorrelationIdAndJobId(correlationId, jobId);
    }
}
