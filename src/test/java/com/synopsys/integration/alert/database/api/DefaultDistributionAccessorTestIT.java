package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class DefaultDistributionAccessorTestIT {

    @Autowired
    private DistributionAccessor distributionAccessor;

    @Autowired
    private DistributionJobRepository distributionJobRepository;

    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @Autowired
    private DescriptorMap descriptorMap;

    private final List<UUID> createdJobs = new LinkedList<>();

    @AfterEach
    public void cleanupDistributions() {
        distributionJobRepository.flush();
        createdJobs.forEach(distributionJobRepository::deleteById);
        createdJobs.clear();

        auditEntryRepository.bulkDeleteOrphanedEntries();
    }

    @Test
    @Transactional
    public void verifyQueryBuildsTest() {
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(0, 100, "name", Direction.ASC, getAllDescriptorNames());

        assertNotNull(distributionWithAuditInfo);
    }

    @Test
    @Transactional
    public void verifyValidityOfQueryTest() {
        assertValidQueryFunctionality(() -> distributionAccessor.getDistributionWithAuditInfo(0, 100, "name", Direction.ASC, getAllDescriptorNames()));
    }

    @Test
    @Transactional
    public void verifyValidityOfQueryWithNullsTest() {
        assertValidQueryFunctionality(() -> distributionAccessor.getDistributionWithAuditInfo(0, 100, null, null, getAllDescriptorNames()));
    }

    @Test
    @Transactional
    public void sortByNameDESCTest() {
        assertSorted("name", Direction.DESC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    @Transactional
    public void sortByNameASCTest() {
        assertSorted("name", Direction.ASC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    @Transactional
    public void sortByAuditLastTimeSentDESCTest() {
        assertAuditLastTimeSent(Direction.DESC);
    }

    @Test
    @Transactional
    public void sortByAuditLastTimeSentASCTest() {
        assertAuditLastTimeSent(Direction.ASC);
    }

    @Test
    @Transactional
    void sortByChannelNameDESCTest() {
        assertSorted("channelDescriptorName", Direction.DESC, DistributionWithAuditInfo::getChannelName);
    }

    private void assertAuditLastTimeSent(Direction sortDirection) {
        assertSorted("lastSent", sortDirection,
            info -> {
                String auditTimeLastSent = info.getAuditTimeLastSent();
                if (StringUtils.isNotBlank(auditTimeLastSent)) {
                    try {
                        return DateUtils.parseDate(auditTimeLastSent, DateUtils.AUDIT_DATE_FORMAT);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        );
    }

    private <T extends Comparable> void assertSorted(String sortName, Direction sortDirection, Function<DistributionWithAuditInfo, T> getNullableValue) {
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfoSorted = assertValidQueryFunctionality(
            () -> distributionAccessor.getDistributionWithAuditInfo(0, 100, sortName, sortDirection, getAllDescriptorNames()));
        T previouslyRetrievedValue = null;
        boolean inOrder = true;
        for (DistributionWithAuditInfo info : distributionWithAuditInfoSorted.getModels()) {
            T retrievedValue = getNullableValue.apply(info);
            if (retrievedValue != null && previouslyRetrievedValue != null) {
                int comparisonResult = previouslyRetrievedValue.compareTo(retrievedValue);
                boolean previousTimeLessThanCurrent = (sortDirection == Direction.ASC) ? comparisonResult <= 0 : comparisonResult >= 0;
                inOrder = inOrder && previousTimeLessThanCurrent;
                if (!inOrder) {
                    break;
                }
            }
            previouslyRetrievedValue = retrievedValue;
        }

        assertTrue(inOrder);
    }

    private Set<String> getAllDescriptorNames() {
        return descriptorMap.getDescriptorKeys().stream().map(DescriptorKey::getUniversalKey).collect(Collectors.toSet());
    }

    private AlertPagedModel<DistributionWithAuditInfo> assertValidQueryFunctionality(Supplier<AlertPagedModel<DistributionWithAuditInfo>> dBQuery) {
        Map<DistributionJobEntity, List<AuditEntryEntity>> jobAndAuditData = createAndSave3JobAndAudit();
        assertEquals(5, jobAndAuditData.keySet().size());

        jobAndAuditData.keySet().stream().map(DistributionJobEntity::getJobId).forEach(uuid -> {
            DistributionJobEntity job = distributionJobRepository.getOne(uuid);
            assertNotNull(job);
        });

        AlertPagedModel<DistributionWithAuditInfo> queryResult = dBQuery.get();
        assertNotNull(queryResult);

        List<DistributionWithAuditInfo> distributionWithAuditInfos = queryResult.getModels();
        assertEquals(6, distributionWithAuditInfos.size());

        for (Map.Entry<DistributionJobEntity, List<AuditEntryEntity>> jobAndAudits : jobAndAuditData.entrySet()) {
            DistributionJobEntity distributionJobEntity = jobAndAudits.getKey();

            Optional<DistributionWithAuditInfo> distributionInfoWithEntity = getDistributionInfo(distributionJobEntity.getJobId(), distributionWithAuditInfos);
            assertTrue(distributionInfoWithEntity.isPresent());

            DistributionWithAuditInfo distributionWithAuditInfo = distributionInfoWithEntity.get();
            assertEquals(distributionJobEntity.getName(), distributionWithAuditInfo.getJobName());
            assertNotEquals(AuditEntryStatus.PENDING.name(), distributionWithAuditInfo.getAuditStatus());

            List<AuditEntryEntity> audits = jobAndAudits.getValue();
            if (!audits.isEmpty()) {
                OffsetDateTime mostRecentAuditEntryTime = audits.stream().max(Comparator.comparing(AuditEntryEntity::getTimeLastSent)).map(AuditEntryEntity::getTimeLastSent).orElse(null);

                String formattedTime = null;
                if (null != mostRecentAuditEntryTime) {
                    formattedTime = DateUtils.formatDate(mostRecentAuditEntryTime, DateUtils.AUDIT_DATE_FORMAT);
                }

                assertEquals(formattedTime, distributionWithAuditInfo.getAuditTimeLastSent());
            }
        }

        return queryResult;
    }

    private Optional<DistributionWithAuditInfo> getDistributionInfo(UUID jobId, List<DistributionWithAuditInfo> items) {
        return items.stream().filter(item -> item.getJobId().equals(jobId)).findFirst();
    }

    private DistributionJobEntity createJobEntity(String channelKey) {
        String jobName = channelKey + UUID.randomUUID();
        return new DistributionJobEntity(
            null,
            jobName,
            true,
            FrequencyType.REAL_TIME.name(),
            ProcessingType.DEFAULT.name(),
            channelKey,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
    }

    private AuditEntryEntity createAuditEntryEntity(UUID commonConfigId, OffsetDateTime timeLastSent, AuditEntryStatus auditEntryStatus) {
        return new AuditEntryEntity(
            commonConfigId,
            OffsetDateTime.now(),
            timeLastSent,
            auditEntryStatus.name(),
            "",
            ""
        );
    }

    private Map<DistributionJobEntity, List<AuditEntryEntity>> createAndSave3JobAndAudit() {
        String slackKey = new SlackChannelKey().getUniversalKey();
        String msTeamsKey = new MsTeamsKey().getUniversalKey();
        DistributionJobEntity firstJob = createJobEntity(slackKey);
        DistributionJobEntity secondJob = createJobEntity(slackKey);
        DistributionJobEntity thirdJob = createJobEntity(slackKey);
        DistributionJobEntity fourthJob = createJobEntity(slackKey);
        DistributionJobEntity fifthJob = createJobEntity(msTeamsKey);
        DistributionJobEntity sixthJob = createJobEntity(msTeamsKey);

        DistributionJobEntity firstJobSaved = distributionJobRepository.save(firstJob);
        DistributionJobEntity secondJobSaved = distributionJobRepository.save(secondJob);
        DistributionJobEntity thirdJobSaved = distributionJobRepository.save(thirdJob);
        DistributionJobEntity fourthJobSaved = distributionJobRepository.save(fourthJob);
        DistributionJobEntity fifthJobSaved = distributionJobRepository.save(fifthJob);
        DistributionJobEntity sixthJobSaved = distributionJobRepository.save(sixthJob);

        createdJobs.add(firstJobSaved.getJobId());
        createdJobs.add(secondJobSaved.getJobId());
        createdJobs.add(thirdJobSaved.getJobId());
        createdJobs.add(fourthJobSaved.getJobId());
        createdJobs.add(fifthJobSaved.getJobId());
        createdJobs.add(sixthJobSaved.getJobId());

        AuditEntryEntity firstAudit = createAuditEntryEntity(firstJob.getJobId(), OffsetDateTime.now(), AuditEntryStatus.SUCCESS);
        AuditEntryEntity secondAudit = createAuditEntryEntity(firstJob.getJobId(), OffsetDateTime.now().minusDays(1), AuditEntryStatus.PENDING);
        AuditEntryEntity thirdAudit = createAuditEntryEntity(secondJob.getJobId(), OffsetDateTime.now().minusMinutes(1), AuditEntryStatus.FAILURE);
        AuditEntryEntity fourthAudit = createAuditEntryEntity(fourthJob.getJobId(), OffsetDateTime.now().minusHours(1), AuditEntryStatus.SUCCESS);
        AuditEntryEntity fifthAudit = createAuditEntryEntity(fifthJob.getJobId(), OffsetDateTime.now().minusHours(2), AuditEntryStatus.SUCCESS);
        AuditEntryEntity sixthAudit = createAuditEntryEntity(fifthJob.getJobId(), OffsetDateTime.now().minusMinutes(2), AuditEntryStatus.FAILURE);
        AuditEntryEntity seventhAudit = createAuditEntryEntity(sixthJob.getJobId(), null, AuditEntryStatus.SUCCESS);
        AuditEntryEntity eighthAudit = createAuditEntryEntity(sixthJob.getJobId(), OffsetDateTime.now(), AuditEntryStatus.FAILURE);
        AuditEntryEntity ninthAudit = createAuditEntryEntity(sixthJob.getJobId(), null, AuditEntryStatus.PENDING);

        saveAllAudits(List.of(firstAudit, secondAudit, thirdAudit, fourthAudit, fifthAudit, sixthAudit, seventhAudit, eighthAudit, ninthAudit));

        return Map.of(
            firstJobSaved, List.of(firstAudit, secondAudit),
            secondJobSaved, List.of(thirdAudit),
            thirdJobSaved, List.of(),
            fourthJobSaved, List.of(fourthAudit),
            fifthJob, List.of(fifthAudit, sixthAudit)
        );
    }

    private List<AuditEntryEntity> saveAllAudits(List<AuditEntryEntity> audits) {
        return auditEntryRepository.saveAll(audits);
    }
}
