package com.synopsys.integration.alert.processing;

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
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SyncTaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.database.api.DefaultConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.NotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationMappingProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.event.JobProcessingEvent;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;

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
    private Gson gson;

    private Long blackDuckGlobalConfigId;
    private UUID channelGlobalConfigId;
    private NotificationContentProcessor notificationContentProcessor;
    private TestProperties properties;

    private Map<UUID, Set<Long>> notificationsDistributed = new HashMap<>();

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
    }

    @AfterEach
    public void cleanUpDB() {
        if (!notificationsDistributed.isEmpty()) {
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
            notificationDetailExtractionDelegator,
            notificationContentProcessor,
            createMockMessageDistributor(),
            lifecycleCaches,
            notificationAccessor,
            jobAccessor,
            jobNotificationMappingAccessor
        );
        JobProcessingEvent event = new JobProcessingEvent(correlationId, jobId);
        eventHandler.handle(event);
        assertFalse(notificationsDistributed.containsKey(jobId));
    }

    @Test
    void testNotificationsForJob() throws IOException {
        DistributionJobModel distributionJobModel = jobAccessor.createJob(createDistributionJobRequest(NotificationType.VULNERABILITY));
        UUID correlationId = UUID.randomUUID();
        UUID jobId = distributionJobModel.getJobId();

        List<AlertNotificationModel> notifications = new ArrayList<>();
        notifications.add(createNotification());
        notifications.add(createNotification());
        notifications = notificationAccessor.saveAllNotifications(notifications);

        notificationProcessor.processNotifications(correlationId, notifications, List.of(distributionJobModel.getDistributionFrequency()));

        ProcessingJobEventHandler eventHandler = new ProcessingJobEventHandler(
            notificationDetailExtractionDelegator,
            notificationContentProcessor,
            createMockMessageDistributor(),
            lifecycleCaches,
            notificationAccessor,
            jobAccessor,
            jobNotificationMappingAccessor
        );
        JobProcessingEvent event = new JobProcessingEvent(correlationId, jobId);
        eventHandler.handle(event);
        assertTrue(notificationsDistributed.containsKey(jobId));
        List<Long> notificationIds = notifications.stream().map(AlertNotificationModel::getId).collect(Collectors.toList());
        assertTrue(notificationsDistributed.get(jobId).containsAll(notificationIds));
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
            "Project Version 1.0"
        );
        ProjectMessage projectMessage = ProjectMessage.componentConcern(providerDetails, project, projectVersion, List.of());
        ProcessedProviderMessage<ProjectMessage> processedProviderMessage = ProcessedProviderMessage.singleSource(notificationModel.getId(), projectMessage);
        return new ProcessedProviderMessageHolder(List.of(processedProviderMessage), List.of());
    }

    private AlertNotificationModel createNotification() throws IOException {
        String content = createVulnerabilityContent();

        return new AlertNotificationModel(
            null,
            blackDuckGlobalConfigId,
            blackDuckProviderKey.getUniversalKey(),
            BLACKDUCK_PROVIDER_NAME,
            NotificationType.VULNERABILITY.name(),
            content,
            OffsetDateTime.now(),
            OffsetDateTime.now().minusMinutes(1),
            true
        );
    }

    private String createVulnerabilityContent() throws IOException {
        String content = TestResourceUtils.readFileToString(VULNERABILITY_SIMPLE_JSON_PATH);
        VulnerabilityNotificationView notificationContent = gson.fromJson(content, VulnerabilityNotificationView.class);
        notificationContent.setCreatedAt(Date.from(OffsetDateTime.now(ZoneOffset.UTC).toInstant()));
        notificationContent.setType(NotificationType.VULNERABILITY);

        return gson.toJson(notificationContent);
    }

    private EventManager createMockEventManager() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        return new EventManager(gson, rabbitTemplate, new SyncTaskExecutor());
    }

    private ProcessingAuditAccessor createMockAuditAccessor() {
        return new ProcessingAuditAccessor() {
            @Override
            public void createOrUpdatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds) {
                Set<Long> currentNotificationIds = notificationsDistributed.computeIfAbsent(jobId, ignored -> new HashSet<>());
                currentNotificationIds.addAll(notificationIds);
            }

            @Override
            public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds) {
                // not used at this point only at the channel level.
            }

            @Override
            public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception) {
                // not used at this point only at the channel level.
            }
        };
    }

    private ProviderMessageDistributor createMockMessageDistributor() {
        EventManager eventManager = createMockEventManager();
        ProcessingAuditAccessor auditAccessor = createMockAuditAccessor();
        return new ProviderMessageDistributor(auditAccessor, eventManager);
    }
}
