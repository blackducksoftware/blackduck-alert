/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.JobNotificationContentProcessor;
import com.blackduck.integration.alert.api.processor.NotificationContentProcessor;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.api.processor.NotificationProcessingLifecycleCache;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.digest.ProjectMessageDigester;
import com.blackduck.integration.alert.api.processor.distribute.DistributionEvent;
import com.blackduck.integration.alert.api.processor.distribute.ProviderMessageDistributor;
import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.api.processor.extract.ProviderMessageExtractionDelegator;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessage;
import com.blackduck.integration.alert.api.processor.extract.model.ProcessedProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.filter.NotificationContentWrapper;
import com.blackduck.integration.alert.api.processor.summarize.ProjectMessageSummarizer;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.database.job.api.DefaultConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.database.job.api.DefaultNotificationAccessor;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.VulnerabilityNotificationView;
import com.google.gson.Gson;

@AlertIntegrationTest
class ProcessingJobEventHandlerTestIT {
    public static final String VULNERABILITY_SIMPLE_JSON_PATH = "json/vulnerabilityNotificationSimple01.json";
    public static final String BLACKDUCK_PROVIDER_NAME = "blackduck-config";
    public static final String TEST_DISTRIBUTION_JOB_NAME = "Test Distribution Job";
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;
    @Autowired
    private DefaultConfigurationModelConfigurationAccessor defaultConfigurationAccessor;
    @Autowired
    private NotificationDetailExtractionDelegator notificationDetailExtractionDelegator;
    @Autowired
    private List<NotificationProcessingLifecycleCache> lifecycleCaches;
    @Autowired
    private DefaultNotificationAccessor notificationAccessor;
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private JobNotificationMappingAccessor jobNotificationMappingAccessor;
    @Autowired
    private EmailGlobalConfigAccessor emailGlobalConfigAccessor;
    @Autowired
    private NotificationMappingProcessor notificationProcessor;
    @Autowired
    private ProjectMessageDigester projectMessageDigester;
    @Autowired
    private ProjectMessageSummarizer projectMessageSummarizer;
    @Autowired
    private Gson gson;

    @Autowired
    private ExecutingJobManager executingJobManager;

    private Long blackDuckGlobalConfigId;
    private UUID channelGlobalConfigId;
    private NotificationContentProcessor notificationContentProcessor;
    private JobNotificationContentProcessor jobNotificationContentProcessor;
    private TestProperties properties;

    private final Map<UUID, Set<String>> jobExecutionIdAndEventIdMap = new HashMap<>();

    @BeforeEach
    public void init() throws AlertConfigurationException {
        properties = new TestProperties();
        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("TRUE");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue(BLACKDUCK_PROVIDER_NAME);
        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue(properties.getBlackDuckURL());
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue(properties.getBlackDuckAPIToken());
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue(String.valueOf(BlackDuckProperties.DEFAULT_TIMEOUT));

        ConfigurationModel blackduckConfigurationModel = defaultConfigurationAccessor.createConfiguration(
            blackDuckProviderKey,
            ConfigContextEnum.GLOBAL,
            List.of(
                providerConfigEnabled,
                providerConfigName,
                blackduckUrl,
                blackduckApiKey,
                blackduckTimeout
            )
        );
        blackDuckGlobalConfigId = blackduckConfigurationModel.getConfigurationId();
        EmailGlobalConfigModel channelConfig = new EmailGlobalConfigModel(UUID.randomUUID().toString(), AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM),
            properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST)
        );

        EmailGlobalConfigModel emailGlobalConfigModel = emailGlobalConfigAccessor.createConfiguration(channelConfig);
        channelGlobalConfigId = UUID.fromString(emailGlobalConfigModel.getId());
        notificationContentProcessor = createContentProcessor();
        jobNotificationContentProcessor = createJobContentProcessor();
    }

    @AfterEach
    public void cleanUpDB() {
        if (!jobExecutionIdAndEventIdMap.isEmpty()) {
            notificationAccessor.deleteNotificationsCreatedBefore(OffsetDateTime.now(ZoneOffset.UTC));
        }
        defaultConfigurationAccessor.deleteConfiguration(blackDuckGlobalConfigId);
        emailGlobalConfigAccessor.deleteConfiguration();
    }

    @Test
    void testNoNotificationsForJob() {
        DistributionJobModel distributionJobModel = jobAccessor.createJob(createDistributionJobRequest(NotificationType.VULNERABILITY));
        UUID correlationId = UUID.randomUUID();
        UUID jobId = distributionJobModel.getJobId();
        ProcessingJobEventHandler eventHandler = new ProcessingJobEventHandler(
            createMockMessageDistributor(),
            lifecycleCaches,
            jobAccessor,
            jobNotificationMappingAccessor,
            jobNotificationContentProcessor,
            executingJobManager
        );
        JobProcessingEvent event = new JobProcessingEvent(correlationId, jobId);
        eventHandler.handle(event);
        assertFalse(jobExecutionIdAndEventIdMap.containsKey(jobId));
    }

    @Test
    void testNotificationsForJob() throws IOException {
        DistributionJobModel distributionJobModel = jobAccessor.createJob(createDistributionJobRequest(NotificationType.VULNERABILITY));
        UUID correlationId = UUID.randomUUID();
        UUID jobId = distributionJobModel.getJobId();

        List<AlertNotificationModel> notifications = new ArrayList<>();
        notifications.add(createNotification(2));
        notifications.add(createNotification(1));
        notifications = notificationAccessor.saveAllNotifications(notifications);

        notificationProcessor.processNotifications(correlationId, notifications, List.of(distributionJobModel.getDistributionFrequency()));

        ProcessingJobEventHandler eventHandler = new ProcessingJobEventHandler(
            createMockMessageDistributor(),
            lifecycleCaches,
            jobAccessor,
            jobNotificationMappingAccessor,
            jobNotificationContentProcessor,
            executingJobManager
        );
        JobProcessingEvent event = new JobProcessingEvent(correlationId, jobId);
        eventHandler.handle(event);
        assertTrue(jobExecutionIdAndEventIdMap.containsKey(jobId));
        // the events in here are Distribution Events where we don't know the ids
        assertEquals(2, jobExecutionIdAndEventIdMap.get(jobId).size());
    }

    private DistributionJobRequestModel createDistributionJobRequest(NotificationType notificationType) {
        EmailJobDetailsModel emailJobDetailsModel = new EmailJobDetailsModel(null, null, false, false, null, List.of());
        return new DistributionJobRequestModel(
            true,
            TEST_DISTRIBUTION_JOB_NAME,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            ChannelKeys.EMAIL.getUniversalKey(),
            channelGlobalConfigId,
            blackDuckGlobalConfigId,
            false,
            "",
            "",
            List.of(notificationType.name()),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            emailJobDetailsModel
        );
    }

    private JobNotificationContentProcessor createJobContentProcessor() {
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator = Mockito.mock(ProviderMessageExtractionDelegator.class);
        Mockito.doAnswer(invocation -> {
                NotificationContentWrapper notifications = invocation.getArgument(0);
                return createNotificationMessageHolder(notifications);
            })
            .when(providerMessageExtractionDelegator).extract(Mockito.any());
        return new JobNotificationContentProcessor(
            notificationDetailExtractionDelegator,
            notificationAccessor,
            jobNotificationMappingAccessor,
            providerMessageExtractionDelegator,
            projectMessageDigester,
            projectMessageSummarizer,
            executingJobManager
        );
    }

    private NotificationContentProcessor createContentProcessor() {
        NotificationContentProcessor notificationContentProcessor = Mockito.mock(NotificationContentProcessor.class);
        Mockito.doAnswer(invocation -> {
                List<NotificationContentWrapper> notifications = invocation.getArgument(1);
                return createMessageHolder(notifications);
            })
            .when(notificationContentProcessor).processNotificationContent(Mockito.any(), Mockito.anyList());

        return notificationContentProcessor;
    }

    private ProcessedProviderMessageHolder createMessageHolder(List<NotificationContentWrapper> notifications) {
        return notifications.stream()
            .map(this::createNotificationMessageHolder)
            .reduce(ProcessedProviderMessageHolder::reduce)
            .orElse(ProcessedProviderMessageHolder.empty());
    }

    private ProcessedProviderMessageHolder createNotificationMessageHolder(NotificationContentWrapper notificationContentWrapper) {
        AlertNotificationModel notificationModel = notificationContentWrapper.getAlertNotificationModel();
        LinkableItem providerItem = new LinkableItem(blackDuckProviderKey.getDisplayName(), notificationModel.getProviderConfigName());
        ProviderDetails providerDetails = new ProviderDetails(notificationModel.getProviderConfigId(), providerItem);

        LinkableItem project = new LinkableItem(BlackDuckMessageLabels.LABEL_PROJECT, "Test Project", null);
        LinkableItem projectVersion = new LinkableItem(
            BlackDuckMessageLabels.LABEL_PROJECT_VERSION,
            String.format("Project Version 1.0 %s", notificationModel.getProviderCreationTime())
        );
        ProjectMessage projectMessage = ProjectMessage.componentConcern(providerDetails, project, projectVersion, List.of());
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = ProcessedProviderMessage.singleSource(notificationModel.getId(), projectMessage);
        return new ProcessedProviderMessageHolder(List.of(processedProviderMessage), List.of());
    }

    private AlertNotificationModel createNotification(int minuteOffset) throws IOException {
        VulnerabilityNotificationView notificationView = createVulnerabilityContent();
        String content = createVulnerabilityContent(notificationView);

        return new AlertNotificationModel(
            null,
            blackDuckGlobalConfigId,
            blackDuckProviderKey.getUniversalKey(),
            BLACKDUCK_PROVIDER_NAME,
            NotificationType.VULNERABILITY.name(),
            content,
            OffsetDateTime.now(),
            OffsetDateTime.now().minusMinutes(minuteOffset),
            true,
            String.format("content-id-%s", UUID.randomUUID()),
            true

        );
    }

    private VulnerabilityNotificationView createVulnerabilityContent() throws IOException {
        String content = TestResourceUtils.readFileToString(VULNERABILITY_SIMPLE_JSON_PATH);
        VulnerabilityNotificationView notificationContent = gson.fromJson(content, VulnerabilityNotificationView.class);
        notificationContent.setCreatedAt(Date.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
        notificationContent.setType(NotificationType.VULNERABILITY);

        return notificationContent;
    }

    private String createVulnerabilityContent(VulnerabilityNotificationView notificationContent) {
        return gson.toJson(notificationContent);
    }

    private EventManager createMockEventManager() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        EventManager eventManager = Mockito.mock(EventManager.class);
        Mockito.doAnswer(invocation -> {
            DistributionEvent event = invocation.getArgument(0, DistributionEvent.class);
            Set<String> eventIdSet = jobExecutionIdAndEventIdMap.computeIfAbsent(event.getJobId(), ignored -> new HashSet<>());
            eventIdSet.add(event.getEventId());
            return null;
        }).when(eventManager).sendEvent(Mockito.any());
        return eventManager;
    }

    private ProcessingAuditAccessor createMockAuditAccessor() {
        return new ProcessingAuditAccessor() {
            @Override
            public void createOrUpdatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds) {
            }

            @Override
            public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds) {
                // not used at this point only at the channel level.
            }

            @Override
            public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds, OffsetDateTime successTimestamp) {
                // not used at this point only at the channel level.
            }

            @Override
            public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception) {
                // not used at this point only at the channel level.
            }

            @Override
            public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
                // not used at this point only at the channel level.
            }

            @Override
            public void setAuditEntryFailure(
                UUID jobId,
                Set<Long> notificationIds,
                OffsetDateTime failureTimestamp,
                String errorMessage,
                @Nullable String stackTrace
            ) {
                // not used at this point only at the channel level.
            }
        };
    }

    private ProviderMessageDistributor createMockMessageDistributor() {
        EventManager eventManager = createMockEventManager();
        ProcessingAuditAccessor auditAccessor = createMockAuditAccessor();
        return new ProviderMessageDistributor(auditAccessor, eventManager, executingJobManager);
    }
}
