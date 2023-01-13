package com.synopsys.integration.alert.api.distribution.mock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.synopsys.integration.alert.database.job.execution.JobExecutionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStatusDurationsEntity;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStatusEntity;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobExecutionStatusRepository extends MockRepositoryContainer<UUID, JobExecutionStatusEntity> implements JobExecutionRepository {

    private final JobExecutionDurationsRepository jobExecutionStatusDurationsRepository;

    public MockJobExecutionStatusRepository(JobExecutionDurationsRepository jobExecutionStatusDurationsRepository) {
        super(JobExecutionStatusEntity::getJobConfigId);
        this.jobExecutionStatusDurationsRepository = jobExecutionStatusDurationsRepository;
    }

    @Override
    public @NotNull Optional<JobExecutionStatusEntity> findById(@NotNull UUID uuid) {
        Optional<JobExecutionStatusEntity> entity = super.findById(uuid);
        return entity.map(this::addDurationData);
    }

    @Override
    public @NotNull List<JobExecutionStatusEntity> findAll() {
        return super.findAll().stream()
            .map(this::addDurationData)
            .collect(Collectors.toList());
    }

    @Override
    public Page<JobExecutionStatusEntity> findBySearchTerm(String searchTerm, Pageable pageable) {
        return Page.empty();
    }

    public JobExecutionStatusEntity addDurationData(JobExecutionStatusEntity currentEntity) {
        JobExecutionStatusDurationsEntity durations = jobExecutionStatusDurationsRepository.findById(currentEntity.getJobConfigId())
            .orElse(new JobExecutionStatusDurationsEntity(currentEntity.getJobConfigId(), 0L, 0L, 0L, 0L, 0L, 0L));
        return new JobExecutionStatusEntity(
            currentEntity.getJobConfigId(),
            currentEntity.getNotificationCount(),
            currentEntity.getSuccessCount(),
            currentEntity.getFailureCount(),
            currentEntity.getLatestStatus(),
            currentEntity.getLastRun(),
            durations
        );
    }
}
