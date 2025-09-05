/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingJobAccessor2;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

class JobNotificationMapper2Test {
    private AtomicLong notificationId = new AtomicLong(1);

    @Test
    void testNoMatchingJobsToMap() {
        UUID correlationId = UUID.randomUUID();
        String project = "project-1";
        String projectVersion = "version-1";
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of());
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        AlertNotificationModel notificationModel = new AlertNotificationModel(
            1L,
            1L,
            "provider",
            "providerConfigName",
            NotificationType.PROJECT.name(),
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();

        jobNotificationMapper.mapJobsToNotifications(
            correlationId,
            List.of(DetailedNotificationContent.project(notificationModel, projectVersionNotificationContent, project, projectVersion)),
            List.of(FrequencyType.REAL_TIME)
        );

        assertFalse(jobNotificationMappingAccessor.hasJobMappings(correlationId));
    }

    @Test
    void testMappingJobsWithoutProjectFilter() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String project = "project-1";
        String projectVersion = "version-1";
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of(createFilteredJobResponse(jobId, false, false, "", "")));
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        AlertNotificationModel notificationModel = new AlertNotificationModel(
            1L,
            1L,
            "provider",
            "providerConfigName",
            NotificationType.PROJECT.name(),
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();

        jobNotificationMapper.mapJobsToNotifications(
            correlationId,
            List.of(DetailedNotificationContent.project(notificationModel, projectVersionNotificationContent, project, projectVersion)),
            List.of(FrequencyType.REAL_TIME)
        );

        assertTrue(jobNotificationMappingAccessor.hasJobMappings(correlationId));
        assertEquals(1, jobNotificationMappingAccessor.getUniqueJobIds(correlationId).size());
    }

    @Test
    void testMappingJobsWithProjectFilter() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String project = "project-1";
        String projectVersion = "version-1";
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of(createFilteredJobResponse(jobId, true, true, "", "")));
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        AlertNotificationModel notificationModel = new AlertNotificationModel(
            1L,
            1L,
            "provider",
            "providerConfigName",
            NotificationType.PROJECT.name(),
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();

        jobNotificationMapper.mapJobsToNotifications(
            correlationId,
            List.of(DetailedNotificationContent.project(notificationModel, projectVersionNotificationContent, project, projectVersion)),
            List.of(FrequencyType.REAL_TIME)
        );

        assertTrue(jobNotificationMappingAccessor.hasJobMappings(correlationId));
        assertEquals(1, jobNotificationMappingAccessor.getUniqueJobIds(correlationId).size());
    }

    @Test
    void testMappingJobsByProjectPattern() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String goodProjectPattern = "^project-\\d$";
        String project = "project-1";
        String projectVersion = "version-1";
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of(createFilteredJobResponse(jobId, true, false, goodProjectPattern, "")));
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        AlertNotificationModel notificationModel = new AlertNotificationModel(
            1L,
            1L,
            "provider",
            "providerConfigName",
            NotificationType.PROJECT.name(),
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();

        jobNotificationMapper.mapJobsToNotifications(
            correlationId,
            List.of(DetailedNotificationContent.project(notificationModel, projectVersionNotificationContent, project, projectVersion)),
            List.of(FrequencyType.REAL_TIME)
        );

        assertTrue(jobNotificationMappingAccessor.hasJobMappings(correlationId));
        assertEquals(1, jobNotificationMappingAccessor.getUniqueJobIds(correlationId).size());
    }

    @Test
    void testMappingJobsByProjectVersionPattern() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String goodProjectVersionPattern = "^version-\\d$";
        String project = "project-1";
        String projectVersion = "version-1";
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of(createFilteredJobResponse(jobId, true, true, "", goodProjectVersionPattern)));
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        AlertNotificationModel notificationModel = new AlertNotificationModel(
            1L,
            1L,
            "provider",
            "providerConfigName",
            NotificationType.PROJECT.name(),
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();

        jobNotificationMapper.mapJobsToNotifications(
            correlationId,
            List.of(DetailedNotificationContent.project(notificationModel, projectVersionNotificationContent, project, projectVersion)),
            List.of(FrequencyType.REAL_TIME)
        );

        assertTrue(jobNotificationMappingAccessor.hasJobMappings(correlationId));
        assertEquals(1, jobNotificationMappingAccessor.getUniqueJobIds(correlationId).size());
    }

    @Test
    void testTotalCountOfNotifications() {
        UUID correlationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        String project = "project-1";
        String projectVersion = "version-1";
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of(createFilteredJobResponse(jobId, false, false, "", "")));
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        AlertNotificationModel notificationModel = new AlertNotificationModel(
            1L,
            1L,
            "provider",
            "providerConfigName",
            NotificationType.PROJECT.name(),
            "",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();

        jobNotificationMapper.mapJobsToNotifications(
            correlationId,
            List.of(DetailedNotificationContent.project(notificationModel, projectVersionNotificationContent, project, projectVersion)),
            List.of(FrequencyType.REAL_TIME)
        );

        assertTrue(jobNotificationMappingAccessor.hasJobMappings(correlationId));
        assertEquals(1, jobNotificationMappingAccessor.getNotificationCountForJob(correlationId, jobId));
    }

    private ProcessingJobAccessor2 createProcessingAccessor(List<SimpleFilteredDistributionJobResponseModel> results) {
        return new ProcessingJobAccessor2() {
            @Override
            public AlertPagedModel<SimpleFilteredDistributionJobResponseModel> getMatchingEnabledJobsForNotifications(
                FilteredDistributionJobRequestModel filteredDistributionJobRequestModel,
                int pageOffset,
                int pageLimit
            ) {
                int total = results.size() / pageLimit;
                return new AlertPagedModel<>(total, pageOffset, pageLimit, results);
            }
        };
    }

    private SimpleFilteredDistributionJobResponseModel createFilteredJobResponse(
        UUID jobId,
        boolean filterByProject,
        boolean matchedProjectNames,
        String projectNamePattern,
        String projectVersionPattern
    ) {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = new SimpleFilteredDistributionJobResponseModel(
            notificationId.incrementAndGet(),
            jobId,
            filterByProject,
            projectNamePattern,
            projectVersionPattern,
            matchedProjectNames
        );
        return jobResponseModel;
    }

    private JobNotificationMappingAccessor createJobNotificationMappingAccessor() {
        return new JobNotificationMappingAccessor() {
            private Map<UUID, Map<UUID, List<JobToNotificationMappingModel>>> dataMap = new HashMap<>();

            @Override
            public AlertPagedModel<JobToNotificationMappingModel> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize) {
                List<JobToNotificationMappingModel> mappings = dataMap.getOrDefault(correlationId, Map.of())
                    .getOrDefault(jobId, List.of());
                int total = mappings.size() / pageSize;
                int start = page * pageSize;
                int end = start + pageSize;

                List<JobToNotificationMappingModel> subList = new ArrayList<>(pageSize);
                for (int index = start; index < end; index++) {
                    try {
                        JobToNotificationMappingModel model = mappings.get(index);
                        subList.add(model);
                    } catch (IndexOutOfBoundsException ex) {
                        // ignore and create an empty page
                    }
                }
                return new AlertPagedModel<>(total, page, pageSize, subList);
            }

            @Override
            public Set<UUID> getUniqueJobIds(UUID correlationId) {
                return dataMap.getOrDefault(correlationId, Map.of()).values().stream()
                    .flatMap(List::stream)
                    .map(JobToNotificationMappingModel::getJobId)
                    .collect(Collectors.toSet());
            }

            @Override
            public boolean hasJobMappings(UUID correlationId) {
                return dataMap.getOrDefault(correlationId, Map.of()).size() > 0;
            }

            @Override
            public void addJobMappings(List<JobToNotificationMappingModel> jobMappings) {
                for (JobToNotificationMappingModel mappingModel : jobMappings) {
                    Map<UUID, List<JobToNotificationMappingModel>> jobMap = dataMap.computeIfAbsent(mappingModel.getCorrelationId(), ignored -> new HashMap<>());
                    List<JobToNotificationMappingModel> jobNotifications = jobMap.computeIfAbsent(mappingModel.getJobId(), ignored -> new LinkedList<>());
                    jobNotifications.add(mappingModel);
                }
            }

            @Override
            public void removeJobMapping(UUID correlationId, UUID jobId) {
                dataMap.getOrDefault(correlationId, Map.of())
                    .getOrDefault(jobId, List.of())
                    .clear();
                dataMap.getOrDefault(correlationId, Map.of()).remove(jobId);
                dataMap.remove(correlationId);
            }

            @Override
            public int getNotificationCountForJob(UUID correlationId, UUID jobId) {
                return dataMap.getOrDefault(correlationId, Map.of())
                    .getOrDefault(jobId, List.of())
                    .size();
            }

            @Override
            public int getCountByCorrelationId(UUID correlationId) {
                return dataMap.values()
                    .stream()
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(Collection::size)
                    .reduce(Integer::sum)
                    .orElse(0);
            }
        };
    }

}
