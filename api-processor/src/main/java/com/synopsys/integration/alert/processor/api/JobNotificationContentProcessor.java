package com.synopsys.integration.alert.processor.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractionDelegator;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;

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

        ProcessingType jobProcessingType = job.getProcessingType();
        while (jobNotificationMappings.getCurrentPage() <= jobNotificationMappings.getTotalPages()) {
            List<Long> notificationIds = extractNotificationIds(jobNotificationMappings);
            List<AlertNotificationModel> notifications = notificationAccessor.findByIds(notificationIds);
            executingJobManager.incrementNotificationCount(jobExecutionId, notifications.size());
            logNotifications("Start", event, notificationIds);
            List<NotificationContentWrapper> notificationContentList = notifications
                .stream()
                .map(notificationDetailExtractionDelegator::wrapNotification)
                .flatMap(List::stream)
                .map(DetailedNotificationContent::getNotificationContentWrapper)
                .collect(Collectors.toList());

            ProcessedProviderMessageHolder extractedProviderMessages = notificationContentList
                .stream()
                .map(providerMessageExtractionDelegator::extract)
                .reduce(ProcessedProviderMessageHolder::reduce)
                .orElse(ProcessedProviderMessageHolder.empty());
            processedMessageHolder = defaultProcessing(processedMessageHolder, extractedProviderMessages);

            if (ProcessingType.DIGEST == jobProcessingType || ProcessingType.SUMMARY == jobProcessingType) {
                processedMessageHolder = digestProcessing(processedMessageHolder);
            }
            pageNumber++;
            jobNotificationMappings = jobNotificationMappingAccessor.getJobNotificationMappings(
                correlationId,
                jobId,
                pageNumber,
                pageSize
            );
            logNotifications("Finished", event, notificationIds);
        }

        if (ProcessingType.SUMMARY == jobProcessingType) {
            processedMessageHolder = summaryProcessing(processedMessageHolder);
        }

        return processedMessageHolder;
    }

    private List<Long> extractNotificationIds(AlertPagedModel<JobToNotificationMappingModel> pageOfMappingData) {
        return pageOfMappingData.getModels().stream()
            .map(JobToNotificationMappingModel::getNotificationId)
            .collect(Collectors.toList());
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
