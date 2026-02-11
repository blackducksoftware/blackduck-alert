/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.digest.ProjectMessageDigester;
import com.blackduck.integration.alert.api.processor.distribute.ProcessedNotificationDetails;
import com.blackduck.integration.alert.api.processor.distribute.ProviderMessageDistributor;
import com.blackduck.integration.alert.api.processor.extract.ProviderMessageExtractionDelegator;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.summarize.ProjectMessageSummarizer;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.common.system.SystemInfo;
import com.blackduck.integration.alert.common.system.SystemInfoReader;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.alert.provider.blackduck.processor.detail.RuleViolationNotificationDetailExtractor;
import com.blackduck.integration.alert.provider.blackduck.processor.message.RuleViolationNotificationMessageExtractor;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.blackduck.integration.alert.test.common.blackduck.BlackDuckResponseTestUtility;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class JobNotificationProcessorTest {
    private static final Gson GSON = new GsonBuilder().create();
    private static final BlackDuckResponseResolver RESPONSE_RESOLVER = new BlackDuckResponseResolver(GSON);

    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Black Duck", "bd-server", "https://bd-server"));
    private static final String CHANNEL_KEY = ChannelKeys.SLACK.getUniversalKey();
    private final BlackDuckResponseTestUtility blackDuckResponseTestUtility = new BlackDuckResponseTestUtility();

    private final UUID uuid = UUID.randomUUID();

    private final UUID jobExecutionId = UUID.randomUUID();
    private final Long notificationId = 123L;

    @Test
    void processNotificationForJobTest() throws IntegrationException {
        // Set up dependencies for JobNotificationProcessor
        RuleViolationNotificationDetailExtractor ruleViolationNotificationDetailExtractor = new RuleViolationNotificationDetailExtractor();
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = new NotificationDetailExtractionDelegator(
            RESPONSE_RESOLVER,
            List.of(ruleViolationNotificationDetailExtractor)
        );

        RuleViolationNotificationMessageExtractor ruleViolationNotificationMessageExtractor = createRuleViolationNotificationMessageExtractor();
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator = new ProviderMessageExtractionDelegator(List.of(ruleViolationNotificationMessageExtractor));
        ProjectMessageDigester projectMessageDigester = new ProjectMessageDigester();
        ProjectMessageSummarizer projectMessageSummarizer = new ProjectMessageSummarizer();
        NotificationContentProcessor notificationContentProcessor = new NotificationContentProcessor(
            providerMessageExtractionDelegator,
            projectMessageDigester,
            projectMessageSummarizer
        );

        MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);
        ProviderMessageDistributor providerMessageDistributor = new ProviderMessageDistributor(processingAuditAccessor, eventManager, executingJobManager);

        NotificationExtractorBlackDuckServicesFactoryCache lifecycleCaches = createNotificationExtractorBlackDuckServicesFactoryCache();

        //Create Requirements for processNotificationForJob
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(jobExecutionId, uuid, CHANNEL_KEY, "JobName");

        AlertNotificationModel notificationModel = createNotification(NotificationType.RULE_VIOLATION.name());

        // Run test and verify notification saved by ProcessingAuditAccessor

        JobNotificationProcessor jobNotificationProcessor = new JobNotificationProcessor(notificationDetailExtractionDelegator, notificationContentProcessor, providerMessageDistributor, List.of(lifecycleCaches));
        jobNotificationProcessor.processNotificationForJob(processedNotificationDetails, ProcessingType.DEFAULT, List.of(notificationModel));

        Mockito.verify(eventManager, Mockito.times(1)).sendEvent(Mockito.any());
    }

    private RuleViolationNotificationMessageExtractor createRuleViolationNotificationMessageExtractor() throws IntegrationException {
        BlackDuckProviderKey providerKey = new BlackDuckProviderKey();

        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        BlackDuckPolicySeverityConverter blackDuckPolicySeverityConverter = new BlackDuckPolicySeverityConverter();
        BlackDuckPolicyComponentConcernCreator blackDuckPolicyComponentConcernCreator = new BlackDuckPolicyComponentConcernCreator(blackDuckPolicySeverityConverter);
        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator = new BlackDuckComponentVulnerabilityDetailsCreator();
        BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory = new BlackDuckComponentPolicyDetailsCreatorFactory(blackDuckPolicySeverityConverter);
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory = new BlackDuckMessageBomComponentDetailsCreatorFactory(vulnerabilityDetailsCreator, blackDuckComponentPolicyDetailsCreatorFactory);

        //Mocks for AbstractRuleViolationNotificationMessageExtractor
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(servicesFactoryCache.retrieveBlackDuckServicesFactory(Mockito.any())).thenReturn(blackDuckServicesFactory);
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(blackDuckHttpClient);
        Mockito.when(blackDuckHttpClient.getBlackDuckUrl()).thenReturn(new HttpUrl(PROVIDER_DETAILS.getProvider().getUrl().get()));

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);
        ProjectVersionComponentVersionView projectVersionComponentVersionView = blackDuckResponseTestUtility.createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.eq(ProjectVersionComponentVersionView.class))).thenReturn(projectVersionComponentVersionView);

        BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

        return new RuleViolationNotificationMessageExtractor(providerKey, servicesFactoryCache, blackDuckPolicyComponentConcernCreator, detailsCreatorFactory, bomComponent404Handler);
    }

    private NotificationExtractorBlackDuckServicesFactoryCache createNotificationExtractorBlackDuckServicesFactoryCache() {
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        AlertProperties properties = new AlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        SystemInfoReader systemInfo = new SystemInfoReader(GSON);
        BlackDuckPropertiesFactory blackDuckPropertiesFactory = new BlackDuckPropertiesFactory(configurationModelConfigurationAccessor, GSON, properties, proxyManager, systemInfo);

        return new NotificationExtractorBlackDuckServicesFactoryCache(blackDuckPropertiesFactory);
    }

    private AlertNotificationModel createNotification(String notificationType) {
        RuleViolationNotificationView ruleViolationNotificationView = blackDuckResponseTestUtility.createRuleViolationNotificationView("project-name", "project-version-name");
        String notificationContent = GSON.toJson(ruleViolationNotificationView);

        return new AlertNotificationModel(
            123L,
            PROVIDER_DETAILS.getProviderConfigId(),
            PROVIDER_DETAILS.getProvider().getLabel(),
            PROVIDER_DETAILS.getProvider().getValue(),
            notificationType,
            notificationContent,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }
}
