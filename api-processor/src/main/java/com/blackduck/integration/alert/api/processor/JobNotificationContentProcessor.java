/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.digest.ProjectMessageDigester;
import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.api.processor.extract.ProviderMessageExtractionDelegator;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.api.processor.summarize.ProjectMessageSummarizer;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.logging.AlertLoggerFactory;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

@Component
public class JobNotificationContentProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Logger notificationLogger = AlertLoggerFactory.getNotificationLogger(getClass());

    private final NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    private final NotificationAccessor notificationAccessor;
    private final JobNotificationMappingAccessor jobNotificationMappingAccessor;

    private final ProviderMessageExtractionDelegator providerMessageExtractionDelegator;
    private final ProjectMessageDigester projectMessageDigester;
    private final ProjectMessageSummarizer projectMessageSummarizer;
    private final ExecutingJobManager executingJobManager;

    @Autowired
    public JobNotificationContentProcessor(
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator,
        NotificationAccessor notificationAccessor,
        JobNotificationMappingAccessor jobNotificationMappingAccessor,
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator,
        ProjectMessageDigester projectMessageDigester,
        ProjectMessageSummarizer projectMessageSummarizer,
        ExecutingJobManager executingJobManager
    ) {
        this.notificationDetailExtractionDelegator = notificationDetailExtractionDelegator;
        this.notificationAccessor = notificationAccessor;
        this.jobNotificationMappingAccessor = jobNotificationMappingAccessor;
        this.providerMessageExtractionDelegator = providerMessageExtractionDelegator;
        this.projectMessageDigester = projectMessageDigester;
        this.projectMessageSummarizer = projectMessageSummarizer;
        this.executingJobManager = executingJobManager;
    }

    public ProcessedProviderMessageHolder processNotifications(JobProcessingEvent event, UUID jobExecutionId, DistributionJobModel job) {
        ProcessedProviderMessageHolder processedMessageHolder = null;
        UUID correlationId = event.getCorrelationId();
        UUID jobId = event.getJobId();
        int pageNumber = 0;
        int pageSize = 200;
        AlertPagedModel<JobToNotificationMappingModel> jobNotificationMappings = jobNotificationMappingAccessor.getJobNotificationMappings(
            correlationId,
            jobId,
            pageNumber,
            pageSize
        );

        //DEBUG: Get first page of mapping page data
        logger.debug("Processing Notifications for Job. CorrelationId: {}, JobId: {} ", correlationId, jobId);
        logger.debug("Initial Page Data. Total Pages: {}, Current Page: {}, Page Size: {}, Number of Mappings in page: {}",
            jobNotificationMappings.getTotalPages(),
            jobNotificationMappings.getCurrentPage(),
            jobNotificationMappings.getPageSize(),
            jobNotificationMappings.getModels().size());

        ProcessingType jobProcessingType = job.getProcessingType();
        while (jobNotificationMappings.getCurrentPage() <= jobNotificationMappings.getTotalPages()) {

            //DEBUG: Check the page in the loop
            logger.debug("Job notification mapping page: {}", jobNotificationMappings.getCurrentPage());

            List<Long> notificationIds = extractNotificationIds(jobNotificationMappings);
            List<AlertNotificationModel> notifications = notificationAccessor.findByIds(notificationIds);

            //DEBUG: Log the notification count from the notification accessor
            String notificationsIdsFromFoundNotifications = notifications.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            logger.debug("Notifications actually found in Notification Accessor: {}", notificationsIdsFromFoundNotifications);

            logNotifications("Start", event, notificationIds);
            List<NotificationContentWrapper> notificationContentList = notifications
                .stream()
                .map(notificationDetailExtractionDelegator::wrapNotification)
                .flatMap(List::stream)
                .filter(notificationContent -> applyDistributionJobFilters(notificationContent, job))
                .map(DetailedNotificationContent::getNotificationContentWrapper)
                .collect(Collectors.toList());

            //DEBUG: Log the notification ID's after notifications are wrapped and initial filter is run
            String notificationContentListIds = notificationContentList.stream()
                .map(NotificationContentWrapper::getNotificationId)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            logger.debug("Notification content list, Size: {}, Notification IDs: {}", notificationContentList.size(), notificationContentListIds);

            ProcessedProviderMessageHolder extractedProviderMessages = notificationContentList
                .stream()
                .map(providerMessageExtractionDelegator::extract)
                .reduce(ProcessedProviderMessageHolder::reduce)
                .orElse(ProcessedProviderMessageHolder.empty());

            //DEBUG: Log notification ID's after performing reduce
            Set<Long> extractedProviderMessageIdSet = extractedProviderMessages.extractAllNotificationIds();
            String extractedProviderMessageIds = extractedProviderMessageIdSet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            logger.debug("Extracted provider message count content list, Size: {}, Notification IDs: {}",
                extractedProviderMessageIdSet.size(),
                extractedProviderMessageIds);

            processedMessageHolder = defaultProcessing(processedMessageHolder, extractedProviderMessages);

            if (ProcessingType.DIGEST == jobProcessingType || ProcessingType.SUMMARY == jobProcessingType) {
                processedMessageHolder = digestProcessing(processedMessageHolder);
            }
            executingJobManager.incrementProcessedNotificationCount(jobExecutionId, notifications.size());
            pageNumber++;
            jobNotificationMappings = jobNotificationMappingAccessor.getJobNotificationMappings(
                correlationId,
                jobId,
                pageNumber,
                pageSize
            );

            //DEBUG: Log the next page of data
            logger.debug("Next Page Data. Total Pages: {}, Current Page: {}, Page Size: {}, Number of Mappings in page: {}",
                jobNotificationMappings.getTotalPages(),
                jobNotificationMappings.getCurrentPage(),
                jobNotificationMappings.getPageSize(),
                jobNotificationMappings.getModels().size());

            logNotifications("Finished", event, notificationIds);
        }

        if (ProcessingType.SUMMARY == jobProcessingType) {
            processedMessageHolder = summaryProcessing(processedMessageHolder);
        }

        return processedMessageHolder;
    }

    private List<Long> extractNotificationIds(AlertPagedModel<JobToNotificationMappingModel> pageOfMappingData) {
        //DEBUG: Log page data when performing notification ID extraction
        logger.debug("Extracting page of notification IDs. Total Pages: {}, Current Page: {}, Page Size: {}, Number of Mappings in page: {}",
            pageOfMappingData.getTotalPages(),
            pageOfMappingData.getCurrentPage(),
            pageOfMappingData.getPageSize(),
            pageOfMappingData.getModels().size());
        return pageOfMappingData.getModels().stream()
            .map(JobToNotificationMappingModel::getNotificationId)
            .collect(Collectors.toList());
    }

    private boolean applyDistributionJobFilters(DetailedNotificationContent notificationContent, DistributionJobModel distributionJobModel) {
        // Policy related notifications may contain multiple policies in a single notification. A second round of filtering must be performed after
        //  extraction to ensure any policy job filters are correctly applied.
        List<String> policyFilterPolicyNames = distributionJobModel.getPolicyFilterPolicyNames();
        if (!policyFilterPolicyNames.isEmpty()) {
            return policyFilterPolicyNames.contains(notificationContent.getPolicyName().orElse(""));
        }
        return true;
    }

    private void logNotifications(String messagePrefix, JobProcessingEvent event, List<Long> notificationIds) {
        if (logger.isDebugEnabled()) {
            String joinedIds = StringUtils.join(notificationIds, ", ");
            notificationLogger.debug(
                "{} processing job: {} batch: {} {} notifications: {}",
                messagePrefix,
                event.getJobId(),
                event.getCorrelationId(),
                notificationIds.size(),
                joinedIds
            );
        }
    }

    private ProcessedProviderMessageHolder defaultProcessing(ProcessedProviderMessageHolder currentMessageHolder, ProcessedProviderMessageHolder extractedProviderMessages) {
        List<ProcessedProviderMessage<ProjectMessage>> filteredProjectMessages = filterProcessedMessages(extractedProviderMessages.getProcessedProjectMessages());
        List<ProcessedProviderMessage<SimpleMessage>> filteredSimpleMessages = filterProcessedMessages(extractedProviderMessages.getProcessedSimpleMessages());

        if ( extractedProviderMessages.extractAllNotificationIds().size() != filteredProjectMessages.size() + filteredSimpleMessages.size()) {
            Set<Long> extractedNotificationIds = extractedProviderMessages.extractAllNotificationIds();
            logger.debug("Notification Ids were filtered. Count before filter: [{}]", extractedNotificationIds.size());
            logger.debug("Notification IDs extracted: [{}]", extractedNotificationIds);
            logger.debug("Count after filter (Project Message): [{}]", filteredProjectMessages.size());
            logger.debug("Project Message IDs extracted: [{}]", filteredProjectMessages);
            logger.debug("Count after filter (Simple Message): [{}]", filteredSimpleMessages.size());
            logger.debug("Simple Message IDs extracted: [{}]", filteredSimpleMessages);
        }

        ProcessedProviderMessageHolder processedProviderMessageHolder = new ProcessedProviderMessageHolder(filteredProjectMessages, filteredSimpleMessages);
        if (null == currentMessageHolder) {
            return processedProviderMessageHolder;
        }

        return ProcessedProviderMessageHolder.reduce(currentMessageHolder, processedProviderMessageHolder);
    }

    private ProcessedProviderMessageHolder digestProcessing(ProcessedProviderMessageHolder extractedProviderMessages) {
        List<ProcessedProviderMessage<ProjectMessage>> filteredProjectMessages = filterProcessedMessages(extractedProviderMessages.getProcessedProjectMessages());
        List<ProcessedProviderMessage<SimpleMessage>> filteredSimpleMessages = filterProcessedMessages(extractedProviderMessages.getProcessedSimpleMessages());
        List<ProcessedProviderMessage<ProjectMessage>> digestedMessages = projectMessageDigester.digest(filteredProjectMessages);
        return new ProcessedProviderMessageHolder(digestedMessages, filteredSimpleMessages);
    }

    private ProcessedProviderMessageHolder summaryProcessing(ProcessedProviderMessageHolder providerMessages) {
        List<ProcessedProviderMessage<ProjectMessage>> filteredProjectMessages = filterProcessedMessages(providerMessages.getProcessedProjectMessages());
        List<ProcessedProviderMessage<SimpleMessage>> filteredSimpleMessages = filterProcessedMessages(providerMessages.getProcessedSimpleMessages());

        List<ProcessedProviderMessage<SimpleMessage>> summarizedMessages = filteredProjectMessages
            .stream()
            .map(projectMessageSummarizer::summarize)
            .collect(Collectors.toList());
        List<ProcessedProviderMessage<SimpleMessage>> allSimpleMessages = ListUtils.union(filteredSimpleMessages, summarizedMessages);
        return new ProcessedProviderMessageHolder(List.of(), allSimpleMessages);
    }

    private <T> List<T> filterProcessedMessages(List<T> processedProviderMessages) {
        return processedProviderMessages.stream().distinct().collect(Collectors.toList());
    }
}
