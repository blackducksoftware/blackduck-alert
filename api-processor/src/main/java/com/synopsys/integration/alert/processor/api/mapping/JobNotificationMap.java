/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.mapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class JobNotificationMap {
    private Map<UUID, List<JobToNotificationRelation>> jobToNotificationMap = new HashMap<>(10000);

    public void addMapping(UUID correlationId, UUID jobId, Long notificationId) {
        List<JobToNotificationRelation> relations = jobToNotificationMap.getOrDefault(correlationId, new LinkedList<>());
        relations.add(new JobToNotificationRelation(correlationId, jobId, notificationId));
    }

    public List<Long> getNotificationsForJob(UUID correlationId, UUID jobId) {
        List<JobToNotificationRelation> relations = jobToNotificationMap.getOrDefault(correlationId, new LinkedList<>());
        return relations.stream()
            .filter(item -> item.getJobId() == jobId)
            .map(JobToNotificationRelation::getNotificationId)
            .collect(Collectors.toList());
    }
}
