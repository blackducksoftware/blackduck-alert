package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class DefaultDistributionAccessorTestIT {
    private final int TOTAL_NUMBER_OF_RECORDS = 6;

    public static final String SORT_LAST_SENT = "time_last_sent";
    public static final String SORT_STATUS = "status";
    public static final String SORT_CHANNEL = "channel_descriptor_name";
    public static final String SORT_FREQUENCY = "distribution_frequency";
    public static final String SORT_NAME = "name";

    @Autowired
    private DistributionAccessor distributionAccessor;

    @Autowired
    private JobAccessor jobAccessor;

    @Autowired
    private DescriptorMap descriptorMap;

    @Autowired
    private ExecutingJobManager executingJobManager;
    @Autowired
    private JobCompletionStatusModelAccessor jobCompletionStatusAccessor;

    private final List<UUID> createdJobs = new LinkedList<>();

    @AfterEach
    public void cleanupDistributions() {
        cleanUpJobs();
        cleanUpExecutions();
    }

    private void cleanUpJobs() {
        int pageNumber = 1;
        AlertPagedModel<DistributionJobModel> jobs = jobAccessor.getPageOfJobs(pageNumber, 100);
        while (pageNumber <= jobs.getTotalPages()) {
            jobs.getModels()
                .stream()
                .map(DistributionJobModel::getJobId)
                .forEach(jobAccessor::deleteJob);
            pageNumber++;
            jobs = jobAccessor.getPageOfJobs(pageNumber, 100);
        }
    }

    private void cleanUpExecutions() {
        int pageNumber = 1;
        AlertPagedModel<ExecutingJob> executingJobs = executingJobManager.getExecutingJobs(pageNumber, 100);
        while (pageNumber <= executingJobs.getTotalPages()) {
            executingJobs.getModels()
                .stream()
                .map(ExecutingJob::getExecutionId)
                .forEach(executingJobManager::purgeJob);
            pageNumber++;
            executingJobs = executingJobManager.getExecutingJobs(pageNumber, 100);
        }
    }

    @Test
    void verifyQueryBuildsTest() {
        AlertPagedModel<DistributionWithAuditInfo> distributionWithAuditInfo = distributionAccessor.getDistributionWithAuditInfo(
            0,
            100,
            "name",
            Direction.ASC,
            getAllDescriptorNames()
        );

        assertNotNull(distributionWithAuditInfo);
    }

    @Test
    void verifyValidityOfQueryTest() throws ParseException {
        assertValidQueryFunctionality(TOTAL_NUMBER_OF_RECORDS, () -> distributionAccessor.getDistributionWithAuditInfo(0, 100, "name", Direction.ASC, getAllDescriptorNames()));
    }

    @Test
    void verifyValidityOfQueryWithNullsTest() throws ParseException {
        assertValidQueryFunctionality(TOTAL_NUMBER_OF_RECORDS, () -> distributionAccessor.getDistributionWithAuditInfo(0, 100, null, null, getAllDescriptorNames()));
    }

    @Test
    void sortByNameDESCLowPageSizeTest() throws ParseException {
        assertSorted(3, 3, SORT_NAME, Direction.DESC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    void sortByNameDESCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_NAME, Direction.DESC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    void sortByNameASCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_NAME, Direction.ASC, DistributionWithAuditInfo::getJobName);
    }

    @Test
    void sortByFrequencyASCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_FREQUENCY, Direction.ASC, DistributionWithAuditInfo::getFrequencyType);
    }

    @Test
    void sortByFrequencyDESCTest() throws ParseException {
        assertSorted(TOTAL_NUMBER_OF_RECORDS, 100, SORT_FREQUENCY, Direction.DESC, DistributionWithAuditInfo::getFrequencyType);
    }

    @Test
    @Disabled("This feature is not currently supported due to limitations in hibernate")
    void sortByAuditLastTimeSentDESCTest() throws ParseException {
        assertAuditLastTimeSent(Direction.DESC);
    }

    @Test
    @Disabled("This feature is not currently supported due to limitations in hibernate")
    void sortByAuditLastTimeSentASCTest() throws ParseException {
        assertAuditLastTimeSent(Direction.ASC);
    }

    @Test
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
        Map<UUID, DistributionJobModel> jobAndAuditData = createAndSave6JobAndAudit();
        assertEquals(TOTAL_NUMBER_OF_RECORDS, jobAndAuditData.keySet().size());

        jobAndAuditData.keySet().stream().forEach(uuid -> {
            Optional<DistributionJobModel> job = jobAccessor.getJobById(uuid);
            assertTrue(job.isPresent());
        });

        AlertPagedModel<DistributionWithAuditInfo> queryResult = dBQuery.get();
        assertNotNull(queryResult);
        assertEquals(expectedNumberOfResults, queryResult.getModels().size());

        for (DistributionWithAuditInfo distributionWithAuditInfo : queryResult.getModels()) {
            DistributionJobModel distributionJobModel = jobAndAuditData.get(distributionWithAuditInfo.getJobId());

            assertEquals(distributionJobModel.getName(), distributionWithAuditInfo.getJobName());

            Optional<JobCompletionStatusModel> jobExecutionStatusModel = jobCompletionStatusAccessor.getJobExecutionStatus(distributionWithAuditInfo.getJobId());
            if (jobExecutionStatusModel.isPresent()) {
                assertEquals(jobExecutionStatusModel.get().getLatestStatus(), distributionWithAuditInfo.getAuditStatus());
                OffsetDateTime lastRunUTC = DateUtils.fromInstantUTC(jobExecutionStatusModel.get().getLastRun().toInstant());
                String jsonFormattedString = DateUtils.formatDate(lastRunUTC, DateUtils.AUDIT_DATE_FORMAT);
                String removedDashes = StringUtils.replace(jsonFormattedString, "-", "/");
                String formattedTimestamp = StringUtils.substringBeforeLast(removedDashes, ".");
                assertEquals(formattedTimestamp, distributionWithAuditInfo.getAuditTimeLastSent());
            }

        }

        return queryResult;
    }
    ./
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
            UUID.randomUUID(),
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

    private Map<UUID, DistributionJobModel> createAndSave6JobAndAudit() {
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

        ExecutingJob executingJob1 = executingJobManager.startJob(firstJobSaved.getJobId(), 1);
        ExecutingJob executingJob2 = executingJobManager.startJob(secondJobSaved.getJobId(), 2);
        executingJobManager.startJob(thirdJobSaved.getJobId(), 3);
        ExecutingJob executingJob4 = executingJobManager.startJob(fourthJobSaved.getJobId(), 4);
        ExecutingJob executingJob5 = executingJobManager.startJob(fifthJobSaved.getJobId(), 5);
        ExecutingJob executingJob6 = executingJobManager.startJob(sixthJobSaved.getJobId(), 6);

        executingJobManager.updateJobStatus(executingJob1.getExecutionId(), AuditEntryStatus.SUCCESS);
        executingJobManager.endJob(executingJob1.getExecutionId(), Instant.now());
        executingJobManager.updateJobStatus(executingJob2.getExecutionId(), AuditEntryStatus.SUCCESS);
        executingJobManager.endJob(executingJob2.getExecutionId(), Instant.now());
        executingJobManager.updateJobStatus(executingJob4.getExecutionId(), AuditEntryStatus.FAILURE);
        executingJobManager.endJob(executingJob4.getExecutionId(), Instant.now());
        executingJobManager.updateJobStatus(executingJob5.getExecutionId(), AuditEntryStatus.SUCCESS);
        executingJobManager.endJob(executingJob5.getExecutionId(), Instant.now());
        executingJobManager.updateJobStatus(executingJob6.getExecutionId(), AuditEntryStatus.FAILURE);
        executingJobManager.endJob(executingJob6.getExecutionId(), Instant.now());

        return Map.of(
            firstJobSaved.getJobId(), firstJobSaved,
            secondJobSaved.getJobId(), secondJobSaved,
            thirdJobSaved.getJobId(), thirdJobSaved,
            fourthJobSaved.getJobId(), fourthJobSaved,
            fifthJobSaved.getJobId(), fifthJobSaved,
            sixthJobSaved.getJobId(), sixthJobSaved
        );
    }
}
