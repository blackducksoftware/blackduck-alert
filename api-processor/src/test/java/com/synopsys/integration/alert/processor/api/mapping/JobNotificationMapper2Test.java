package com.synopsys.integration.alert.processor.api.mapping;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

class JobNotificationMapper2Test {

    @Test
    void testMappingJobs() {
        UUID correlationId = UUID.randomUUID();
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of());
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);

        jobNotificationMapper.mapJobsToNotifications(correlationId, List.of(), List.of(FrequencyType.REAL_TIME));

        assertFalse(jobNotificationMappingAccessor.hasJobMappings(correlationId));
    }

    @Test
    void testProjectRegularExpression() {
        UUID correlationId = UUID.randomUUID();
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of());
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);
        jobNotificationMapper.mapJobsToNotifications(correlationId, List.of(), List.of(FrequencyType.REAL_TIME));

        assertFalse(jobNotificationMappingAccessor.hasJobMappings(correlationId));
    }

    @Test
    void testProjectVersionRegularExpression() {
        UUID correlationId = UUID.randomUUID();
        ProcessingJobAccessor2 processingJobAccessor = createProcessingAccessor(List.of());
        JobNotificationMappingAccessor jobNotificationMappingAccessor = createJobNotificationMappingAccessor();
        JobNotificationMapper2 jobNotificationMapper = new JobNotificationMapper2(processingJobAccessor, jobNotificationMappingAccessor);
        jobNotificationMapper.mapJobsToNotifications(correlationId, List.of(), List.of(FrequencyType.REAL_TIME));

        assertFalse(jobNotificationMappingAccessor.hasJobMappings(correlationId));
    }

    private ProcessingJobAccessor2 createProcessingAccessor(List<SimpleFilteredDistributionJobResponseModel> results) {
        return new ProcessingJobAccessor2() {
            @Override
            public AlertPagedModel<SimpleFilteredDistributionJobResponseModel> getMatchingEnabledJobsForNotifications(
                FilteredDistributionJobRequestModel filteredDistributionJobRequestModel,
                int pageOffset,
                int pageLimit
            ) {
                return new AlertPagedModel<>(1, 0, results.size(), results);
            }
        };
    }

    private JobNotificationMappingAccessor createJobNotificationMappingAccessor() {
        return new JobNotificationMappingAccessor() {
            private Map<UUID, Map<UUID, List<JobToNotificationMappingModel>>> dataMap = new HashMap<>();

            @Override
            public AlertPagedModel<JobToNotificationMappingModel> getJobNotificationMappings(UUID correlationId, UUID jobId, int page, int pageSize) {
                List<JobToNotificationMappingModel> mappings = dataMap.getOrDefault(correlationId, Map.of())
                    .get(jobId);
                int total = mappings.size() / pageSize;
                int start = page * pageSize;
                int end = start + pageSize;

                List<JobToNotificationMappingModel> subList = new ArrayList<>(pageSize);
                for (int index = start; index < end; index++) {
                    try {
                        JobToNotificationMappingModel model = mappings.get(index);
                        subList.add(model);
                    } catch (IndexOutOfBoundsException ex) {

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
        };
    }

}
