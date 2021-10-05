package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfoGavin = distributionAccessor.getDistributionWithAuditInfoGavin(0, 100, "name");

        assertNotNull(distributionWithAuditInfo);
        assertNotNull(distributionWithAuditInfoGavin);
    }

    @Test
    @Transactional
    public void verifyValidityOfQuery() {
        assertValidQueryFunctionality(() -> distributionAccessor.getDistributionWithAuditInfo(0, 100, "name"));
    }

    @Test
    @Transactional
    public void verifyValidityOfGavinQuery() {
        assertValidQueryFunctionality(() -> distributionAccessor.getDistributionWithAuditInfoGavin(0, 100, "name"));
    }

    @Test
    @Transactional
    public void verifyValidityOfSimpleQuery() {
        assertValidQueryFunctionality(() -> distributionAccessor.getDistributionJobModel(0, 100, "name"));
    }

    @Test
    @Transactional
    public void gavinQueryFaster() {
        List<UUID> manyItems = createManyItems();
        createdJobs.addAll(manyItems);

        long longerRun = runningTime(() -> distributionAccessor.getDistributionWithAuditInfo(0, 100, "name"));
        long quickerRun = runningTime(() -> distributionAccessor.getDistributionWithAuditInfoGavin(0, 100, "name"));

        System.out.println("Normal: " + longerRun);
        System.out.println("Gavin: " + quickerRun);
        assertTrue(quickerRun < longerRun, String.format("normal run: %s, gavin run %s", longerRun, quickerRun));
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
        return items.stream().filter(item -> item.getJobId() == jobId).findFirst();
    }

    private long runningTime(Supplier<AlertPagedModel<DistributionWithAuditInfo>> dBQuery) {
        long startTimer = System.nanoTime();
        dBQuery.get();
        long stopTimer = System.nanoTime();
        return stopTimer - startTimer;
    }

    private DistributionJobEntity createJobEntity() {
        long randomLong = new Random().nextLong();
        String slackKey = new SlackChannelKey().getUniversalKey();
        String jobName = slackKey + randomLong;
        return new DistributionJobEntity(
            UUID.randomUUID(),
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

    private List<UUID> createManyItems() {
        List<DistributionJobEntity> jobs = new LinkedList<>();
        List<AuditEntryEntity> audits = new LinkedList<>();

        IntStream.range(0, 1000).forEach((x) -> {
            DistributionJobEntity jobEntity = createJobEntity();
            jobs.add(jobEntity);
        });

        List<UUID> allCreatedUUIDs = saveAllJobs(jobs).stream().map(DistributionJobEntity::getJobId).collect(Collectors.toList());

        for (int i = 0; i < allCreatedUUIDs.size(); i++) {
            UUID uuid = allCreatedUUIDs.get(i);
            AuditEntryEntity defaultAudit = createAuditEntryEntity(uuid, OffsetDateTime.now().minus(Long.valueOf(i), ChronoUnit.HOURS));
            audits.add(defaultAudit);
            if (i % 2 == 0) {
                AuditEntryEntity evenAudits = createAuditEntryEntity(uuid, OffsetDateTime.now().minus(Long.valueOf(i * 2), ChronoUnit.HOURS));
                audits.add(evenAudits);
            }
            if (i % 3 == 0) {
                AuditEntryEntity thirdsysAudit = createAuditEntryEntity(uuid, OffsetDateTime.now().minus(Long.valueOf(i * 3), ChronoUnit.HOURS));
                audits.add(thirdsysAudit);
            }
            if (i % 7 == 0) {
                AuditEntryEntity sevensysAudit = createAuditEntryEntity(uuid, OffsetDateTime.now().minus(Long.valueOf(i * 7), ChronoUnit.HOURS));
                audits.add(sevensysAudit);
            }
        }

        List<AuditEntryEntity> auditEntryEntities = saveAllAudits(audits);

        System.out.println("Job count: " + allCreatedUUIDs.size());
        System.out.println("Audit count: " + auditEntryEntities.size());

        return allCreatedUUIDs;
    }

    private List<DistributionJobEntity> saveAllJobs(List<DistributionJobEntity> jobs) {
        return distributionJobRepository.saveAll(jobs);
    }

    private List<AuditEntryEntity> saveAllAudits(List<AuditEntryEntity> audits) {
        return auditEntryRepository.saveAll(audits);
    }
}
