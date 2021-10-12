package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class DefaultDistributionAccessorTestIT {

    @Autowired
    private DistributionAccessor distributionAccessor;

    @Autowired
    private DistributionJobRepository distributionJobRepository;

    @Autowired
    private AuditEntryRepository auditEntryRepository;

    private final List<UUID> createdJobs = new LinkedList<>();

    @AfterEach
    public void cleanupDistributions() {
        distributionJobRepository.flush();
        System.out.println(String.format("Deleting %s jobs", createdJobs.size()));
        createdJobs.forEach(distributionJobRepository::deleteById);
        createdJobs.clear();

        auditEntryRepository.bulkDeleteOrphanedEntries();
    }

    @Test
    @Transactional
    public void verifyQueryBuilds() {
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(0, 100, "name");

        assertNotNull(distributionWithAuditInfo);
    }

    @Test
    @Transactional
    public void verifyValidityOfQuery() {
        assertValidQueryFunctionality(() -> distributionAccessor.getDistributionWithAuditInfo(0, 100, "name"));
    }

    private void assertValidQueryFunctionality(Supplier<AlertPagedModel<DistributionWithAuditInfo>> dBQuery) {
        Map<DistributionJobEntity, List<AuditEntryEntity>> jobAndAuditData = createAndSave3JobAndAudit();
        assertEquals(3, jobAndAuditData.keySet().size());

        jobAndAuditData.keySet().stream().map(DistributionJobEntity::getJobId).forEach(uuid -> {
            DistributionJobEntity job = distributionJobRepository.getOne(uuid);
            assertNotNull(job);
        });

        AlertPagedModel<DistributionWithAuditInfo> queryResult = dBQuery.get();
        assertNotNull(queryResult);

        List<DistributionWithAuditInfo> distributionWithAuditInfos = queryResult.getModels();
        assertEquals(3, distributionWithAuditInfos.size());

        distributionWithAuditInfos.forEach(it -> System.out.println(it.getAuditStatus()));

        for (Map.Entry<DistributionJobEntity, List<AuditEntryEntity>> jobAndAudits : jobAndAuditData.entrySet()) {
            DistributionJobEntity distributionJobEntity = jobAndAudits.getKey();

            Optional<DistributionWithAuditInfo> distributionInfoWithEntity = getDistributionInfo(distributionJobEntity.getJobId(), distributionWithAuditInfos);
            assertTrue(distributionInfoWithEntity.isPresent());

            DistributionWithAuditInfo distributionWithAuditInfo = distributionInfoWithEntity.get();
            assertEquals(distributionJobEntity.getName(), distributionWithAuditInfo.getJobName());

            List<AuditEntryEntity> audits = jobAndAudits.getValue();
            if (!audits.isEmpty()) {
                OffsetDateTime mostRecentAuditEntryTime = audits.stream().max(Comparator.comparing(AuditEntryEntity::getTimeLastSent)).map(AuditEntryEntity::getTimeLastSent).orElse(null);
                assertEquals(mostRecentAuditEntryTime, distributionWithAuditInfo.getAuditTimeLastSent());
            }
        }
    }

    private Optional<DistributionWithAuditInfo> getDistributionInfo(UUID jobId, List<DistributionWithAuditInfo> items) {
        return items.stream().filter(item -> item.getJobId().equals(jobId)).findFirst();
    }

    private DistributionJobEntity createJobEntity() {
        long randomLong = new Random().nextLong();
        String slackKey = new SlackChannelKey().getUniversalKey();
        String jobName = slackKey + randomLong;
        return new DistributionJobEntity(
            null,
            jobName,
            true,
            FrequencyType.REAL_TIME.name(),
            ProcessingType.DEFAULT.name(),
            slackKey,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    private AuditEntryEntity createAuditEntryEntity(UUID commonConfigId, OffsetDateTime timeLastSent) {
        return new AuditEntryEntity(
            commonConfigId,
            OffsetDateTime.now(),
            timeLastSent,
            AuditEntryStatus.SUCCESS.name(),
            "",
            ""
        );
    }

    private Map<DistributionJobEntity, List<AuditEntryEntity>> createAndSave3JobAndAudit() {
        DistributionJobEntity firstJob = createJobEntity();
        DistributionJobEntity secondJob = createJobEntity();
        DistributionJobEntity thirdJob = createJobEntity();

        DistributionJobEntity firstJobSaved = distributionJobRepository.save(firstJob);
        DistributionJobEntity secondJobSaved = distributionJobRepository.save(secondJob);
        DistributionJobEntity thirdJobSaved = distributionJobRepository.save(thirdJob);

        createdJobs.add(firstJobSaved.getJobId());
        createdJobs.add(secondJobSaved.getJobId());
        createdJobs.add(thirdJobSaved.getJobId());

        AuditEntryEntity firstAudit = createAuditEntryEntity(firstJob.getJobId(), OffsetDateTime.now());
        AuditEntryEntity secondAudit = createAuditEntryEntity(firstJob.getJobId(), OffsetDateTime.now());
        AuditEntryEntity thirdAudit = createAuditEntryEntity(secondJob.getJobId(), OffsetDateTime.now());

        saveAllAudits(List.of(firstAudit, secondAudit, thirdAudit));

        return Map.of(
            firstJobSaved, List.of(firstAudit, secondAudit),
            secondJobSaved, List.of(thirdAudit),
            thirdJobSaved, List.of()
        );
    }

    private List<AuditEntryEntity> saveAllAudits(List<AuditEntryEntity> audits) {
        return auditEntryRepository.saveAll(audits);
    }
}
