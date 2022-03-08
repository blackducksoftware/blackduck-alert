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
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class DefaultDistributionAccessorTestIT {
    private final int TOTAL_NUMBER_OF_RECORDS = 6;

    public static final String SORT_LAST_SENT = "filteredAudit.time_last_sent";
    public static final String SORT_STATUS = "filteredAudit.status";
    public static final String SORT_CHANNEL = "channel_descriptor_name";
    public static final String SORT_FREQUENCY = "distribution_frequency";
    public static final String SORT_NAME = "name";

    @Autowired
    private DistributionAccessor distributionAccessor;

    @Autowired
    private JobAccessor jobAccessor;

    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @Autowired
    private DescriptorMap descriptorMap;

    private final List<UUID> createdJobs = new LinkedList<>();

    @AfterEach
    public void cleanupDistributions() {
        createdJobs.forEach(jobAccessor::deleteJob);
        createdJobs.clear();
    }

    @Test
    @Transactional
    @Modifying
    public void verifyQueryBuildsTest() {
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(0, 100, "name", Direction.ASC, getAllDescriptorNames());

        assertNotNull(distributionWithAuditInfo);
    }

    @Test
    @Transactional
    @Modifying
    public void verifyValidityOfQueryTest() throws ParseException {
        assertValidQueryFunctionality(TOTAL_NUMBER_OF_RECORDS, () -> distributionAccessor.getDistributionWithAuditInfo(0, 100, "name", Direction.ASC, getAllDescriptorNames()));
    }

    @Test
    @Transactional
    @Modifying
    public void verifyValidityOfQueryWithNullsTest() throws ParseException {
        assertValidQueryFunctionality(TOTAL_NUMBER_OF_RECORDS, () -> distributionAccessor.getDistributionWithAuditInfo(0, 100, null, null, getAllDescriptorNames()));
    }

    @Test
    @Transactional
    @Modifying
    public void sortByNameDESCLowPageSizeTest() throws ParseException {
        assertSorted(3, 3, SORT_NAME, Direction.DESC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    @Transactional
    @Modifying
    public void sortByNameDESCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_NAME, Direction.DESC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    @Transactional
    @Modifying
    public void sortByNameASCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_NAME, Direction.ASC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    @Transactional
    @Modifying
    public void sortByFrequencyASCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_FREQUENCY, Direction.ASC, DistributionWithAuditInfo::getFrequencyType);
    }

    @Test
    @Transactional
    @Modifying
    public void sortByFrequencyDESCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_FREQUENCY, Direction.DESC, DistributionWithAuditInfo::getFrequencyType);
    }

    @Test
    @Transactional
    @Modifying
    @Disabled("This feature is not currently supported due to limitations in hibernate")
    public void sortByAuditLastTimeSentDESCTest() throws ParseException {
        assertAuditLastTimeSent(Direction.DESC);
    }

    @Test
    @Transactional
    @Modifying
    @Disabled("This feature is not currently supported due to limitations in hibernate")
    public void sortByAuditLastTimeSentASCTest() throws ParseException {
        assertAuditLastTimeSent(Direction.ASC);
    }

    @Test
    @Transactional
    @Modifying
    void sortByChannelNameDESCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_CHANNEL, Direction.DESC, DistributionWithAuditInfo::getChannelName);
    }

    private AlertPagedModel<DistributionWithAuditInfo> assertAuditLastTimeSent(Direction sortDirection) throws ParseException {
        return assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_LAST_SENT, sortDirection,
            info -> {
                String auditTimeLastSent = info.getAuditTimeLastSent();
                if (StringUtils.isNotBlank(auditTimeLastSent)) {
                    try {
                        return DateUtils.parseDate(auditTimeLastSent, "yyyy-MM-dd HH:mm:ss");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        );
    }

    private <T extends Comparable> AlertPagedModel<DistributionWithAuditInfo> assertSorted(int expectedTotal, int pageSize, String sortName, Direction sortDirection, Function<DistributionWithAuditInfo, T> getNullableValue)
        throws ParseException {
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfoSorted = assertValidQueryFunctionality(
            expectedTotal,
            () -> distributionAccessor.getDistributionWithAuditInfo(0, pageSize, sortName, sortDirection, getAllDescriptorNames())
        );
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
        return distributionWithAuditInfoSorted;
    }

    private Set<String> getAllDescriptorNames() {
        return descriptorMap.getDescriptorKeys().stream().map(DescriptorKey::getUniversalKey).collect(Collectors.toSet());
    }

    private AlertPagedModel<DistributionWithAuditInfo> assertValidQueryFunctionality(int expectedNumberOfResults, Supplier<AlertPagedModel<DistributionWithAuditInfo>> dBQuery) throws ParseException {
        Map<UUID, Pair<DistributionJobModel, List<AuditEntryEntity>>> jobAndAuditData = createAndSave6JobAndAudit();
        assertEquals(TOTAL_NUMBER_OF_RECORDS, jobAndAuditData.keySet().size());

        jobAndAuditData.keySet().stream().forEach(uuid -> {
            Optional<DistributionJobModel> job = jobAccessor.getJobById(uuid);
            assertTrue(job.isPresent());
        });

        AlertPagedModel<DistributionWithAuditInfo> queryResult = dBQuery.get();
        assertNotNull(queryResult);
        assertEquals(expectedNumberOfResults, queryResult.getModels().size());

        for (DistributionWithAuditInfo distributionWithAuditInfo : queryResult.getModels()) {
            Pair<DistributionJobModel, List<AuditEntryEntity>> distributionJobModelListPair = jobAndAuditData.get(distributionWithAuditInfo.getJobId());
            DistributionJobModel distributionJobModel = distributionJobModelListPair.getLeft();

            assertEquals(distributionJobModel.getName(), distributionWithAuditInfo.getJobName());
            assertNotEquals(AuditEntryStatus.PENDING.name(), distributionWithAuditInfo.getAuditStatus());

            List<AuditEntryEntity> audits = distributionJobModelListPair.getRight();
            if (!audits.isEmpty()) {
                OffsetDateTime mostRecentAuditEntryTime = audits.stream().filter(auditEntryEntity -> auditEntryEntity.getTimeLastSent() != null).max(Comparator.comparing(AuditEntryEntity::getTimeLastSent))
                    .map(AuditEntryEntity::getTimeLastSent).orElse(null);

                String formattedTime = null;
                if (null != mostRecentAuditEntryTime) {
                    formattedTime = DateUtils.formatDate(mostRecentAuditEntryTime, DateUtils.AUDIT_DATE_FORMAT);
                }

                assertEquals(formattedTime, distributionWithAuditInfo.getAuditTimeLastSent());
            }
        }

        return queryResult;
    }

    private DistributionJobRequestModel createSlackJob(boolean realTime) {
        SlackJobDetailsModel slackJobDetailsModel = new SlackJobDetailsModel(
            null,
            "webhook",
            "channel",
            "username"
        );
        return createJobEntity(new SlackChannelKey().getUniversalKey(), realTime, slackJobDetailsModel);
    }

    private DistributionJobRequestModel createMSTeamsJob(boolean realTime) {
        MSTeamsJobDetailsModel msTeamsJobDetailsModel = new MSTeamsJobDetailsModel(
            null,
            "webhook"
        );
        return createJobEntity(new MsTeamsKey().getUniversalKey(), realTime, msTeamsJobDetailsModel);
    }

    private DistributionJobRequestModel createJobEntity(String channelKey, boolean realTime, DistributionJobDetailsModel jobDetailsModel) {
        String jobName = channelKey + UUID.randomUUID();
        return new DistributionJobRequestModel(
            true,
            jobName,
            realTime ? FrequencyType.REAL_TIME : FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            channelKey,
            1L,
            false,
            null,
            null,
            List.of("LICENSE_LIMIT"),
            List.of(),
            List.of(),
            List.of(),
            jobDetailsModel
        );
    }

    private AuditEntryEntity createAuditEntryEntity(UUID commonConfigId, OffsetDateTime timeLastSent, AuditEntryStatus auditEntryStatus) {
        String statusName = (auditEntryStatus == null) ? null : auditEntryStatus.name();
        return new AuditEntryEntity(
            commonConfigId,
            OffsetDateTime.now(),
            timeLastSent,
            statusName,
            "",
            ""
        );
    }

    private Map<UUID, Pair<DistributionJobModel, List<AuditEntryEntity>>> createAndSave6JobAndAudit() {
        DistributionJobRequestModel firstJob = createSlackJob(true);
        DistributionJobRequestModel secondJob = createSlackJob(false);
        DistributionJobRequestModel thirdJob = createSlackJob(true);
        DistributionJobRequestModel fourthJob = createSlackJob(false);
        DistributionJobRequestModel fifthJob = createMSTeamsJob(true);
        DistributionJobRequestModel sixthJob = createMSTeamsJob(false);

        DistributionJobModel firstJobSaved = jobAccessor.createJob(firstJob);
        DistributionJobModel secondJobSaved = jobAccessor.createJob(secondJob);
        DistributionJobModel thirdJobSaved = jobAccessor.createJob(thirdJob);
        DistributionJobModel fourthJobSaved = jobAccessor.createJob(fourthJob);
        DistributionJobModel fifthJobSaved = jobAccessor.createJob(fifthJob);
        DistributionJobModel sixthJobSaved = jobAccessor.createJob(sixthJob);

        createdJobs.add(firstJobSaved.getJobId());
        createdJobs.add(secondJobSaved.getJobId());
        createdJobs.add(thirdJobSaved.getJobId());
        createdJobs.add(fourthJobSaved.getJobId());
        createdJobs.add(fifthJobSaved.getJobId());
        createdJobs.add(sixthJobSaved.getJobId());

        AuditEntryEntity firstAudit = createAuditEntryEntity(firstJobSaved.getJobId(), OffsetDateTime.now(), AuditEntryStatus.SUCCESS);
        AuditEntryEntity secondAudit = createAuditEntryEntity(firstJobSaved.getJobId(), OffsetDateTime.now().minusDays(1), AuditEntryStatus.PENDING);
        AuditEntryEntity thirdAudit = createAuditEntryEntity(secondJobSaved.getJobId(), OffsetDateTime.now().minusMinutes(1), AuditEntryStatus.FAILURE);
        AuditEntryEntity fourthAudit = createAuditEntryEntity(fourthJobSaved.getJobId(), OffsetDateTime.now().minusHours(1), AuditEntryStatus.SUCCESS);
        AuditEntryEntity fifthAudit = createAuditEntryEntity(fifthJobSaved.getJobId(), OffsetDateTime.now().minusHours(2), AuditEntryStatus.SUCCESS);
        AuditEntryEntity sixthAudit = createAuditEntryEntity(fifthJobSaved.getJobId(), OffsetDateTime.now().minusMinutes(2), AuditEntryStatus.FAILURE);
        AuditEntryEntity seventhAudit = createAuditEntryEntity(sixthJobSaved.getJobId(), null, AuditEntryStatus.SUCCESS);
        AuditEntryEntity eighthAudit = createAuditEntryEntity(sixthJobSaved.getJobId(), OffsetDateTime.now(), AuditEntryStatus.FAILURE);
        AuditEntryEntity ninthAudit = createAuditEntryEntity(sixthJobSaved.getJobId(), null, AuditEntryStatus.PENDING);

        saveAllAudits(List.of(firstAudit, secondAudit, thirdAudit, fourthAudit, fifthAudit, sixthAudit, seventhAudit, eighthAudit, ninthAudit));

        return Map.of(
            firstJobSaved.getJobId(), Pair.of(firstJobSaved, List.of(firstAudit, secondAudit)),
            secondJobSaved.getJobId(), Pair.of(secondJobSaved, List.of(thirdAudit)),
            thirdJobSaved.getJobId(), Pair.of(thirdJobSaved, List.of()),
            fourthJobSaved.getJobId(), Pair.of(fourthJobSaved, List.of(fourthAudit)),
            fifthJobSaved.getJobId(), Pair.of(fifthJobSaved, List.of(fifthAudit, sixthAudit)),
            sixthJobSaved.getJobId(), Pair.of(sixthJobSaved, List.of(seventhAudit, eighthAudit, ninthAudit))
        );
    }

    private List<AuditEntryEntity> saveAllAudits(List<AuditEntryEntity> audits) {
        return auditEntryRepository.saveAll(audits);
    }
}
