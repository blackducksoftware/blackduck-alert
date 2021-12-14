package com.synopsys.integration.alert.processor.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.distribute.ProcessedNotificationDetails;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractionDelegator;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.detail.RuleViolationNotificationDetailExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.message.RuleViolationNotificationMessageExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.alert.test.common.blackduck.BlackDuckResponseTestUtility;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class JobNotificationProcessorTest {
    private static final Gson GSON = new GsonBuilder().create();
    private static final BlackDuckResponseResolver RESPONSE_RESOLVER = new BlackDuckResponseResolver(GSON);

    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Black Duck", "bd-server", "https://bd-server"));
    private static final String CHANNEL_KEY = ChannelKeys.SLACK.getUniversalKey();
    private final BlackDuckResponseTestUtility blackDuckResponseTestUtility = new BlackDuckResponseTestUtility();

    private final UUID uuid = UUID.randomUUID();
    private final Long notificationId = 123L;

    @Test
    public void processNotificationForJobTest() throws IntegrationException {
        // Set up dependencies for JobNotificationProcessor
        RuleViolationNotificationDetailExtractor ruleViolationNotificationDetailExtractor = new RuleViolationNotificationDetailExtractor();
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = new NotificationDetailExtractionDelegator(RESPONSE_RESOLVER, List.of(ruleViolationNotificationDetailExtractor));

        RuleViolationNotificationMessageExtractor ruleViolationNotificationMessageExtractor = createRuleViolationNotificationMessageExtractor();
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator = new ProviderMessageExtractionDelegator(List.of(ruleViolationNotificationMessageExtractor));
        ProjectMessageDigester projectMessageDigester = new ProjectMessageDigester();
        ProjectMessageSummarizer projectMessageSummarizer = new ProjectMessageSummarizer();
        NotificationContentProcessor notificationContentProcessor = new NotificationContentProcessor(providerMessageExtractionDelegator, projectMessageDigester, projectMessageSummarizer);

        MockProcessingAuditAccessor processingAuditAccessor = new MockProcessingAuditAccessor();
        EventManager eventManager = Mockito.mock(EventManager.class);
        ProviderMessageDistributor providerMessageDistributor = new ProviderMessageDistributor(processingAuditAccessor, eventManager);

        NotificationExtractorBlackDuckServicesFactoryCache lifecycleCaches = createNotificationExtractorBlackDuckServicesFactoryCache();

        //Create Requirements for processNotificationForJob
        ProcessedNotificationDetails processedNotificationDetails = new ProcessedNotificationDetails(uuid, CHANNEL_KEY, "JobName");

        AlertNotificationModel notificationModel = createNotification(NotificationType.RULE_VIOLATION.name());

        // Run test and verify notification saved by ProcessingAuditAccessor

        JobNotificationProcessor jobNotificationProcessor = new JobNotificationProcessor(notificationDetailExtractionDelegator, notificationContentProcessor, providerMessageDistributor, List.of(lifecycleCaches));
        jobNotificationProcessor.processNotificationForJob(processedNotificationDetails, ProcessingType.DEFAULT, List.of(notificationModel));

        Set<Long> auditNotificationIds = processingAuditAccessor.getNotificationIds(uuid);

        Mockito.verify(eventManager, Mockito.times(1)).sendEvent(Mockito.any());
        assertEquals(1, auditNotificationIds.size());
        assertTrue(auditNotificationIds.contains(notificationId));
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
        BlackDuckPropertiesFactory blackDuckPropertiesFactory = new BlackDuckPropertiesFactory(configurationModelConfigurationAccessor, GSON, properties, proxyManager);

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
            false
        );
    }
}
