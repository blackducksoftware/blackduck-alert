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
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
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
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
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
    private static final LinkableItem COMPONENT = new LinkableItem("Component", "BOM component name");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final String LICENSE_DISPLAY = "licenseDisplay";
    private static final String CHANNEL_KEY = ChannelKeys.SLACK.getUniversalKey();

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

        RuleViolationNotificationView ruleViolationNotificationView = createRuleViolationNotificationView("TestProjectName");
        String notificationContentString = GSON.toJson(ruleViolationNotificationView);
        AlertNotificationModel notificationModel = createNotification(NotificationType.RULE_VIOLATION.name(), notificationContentString);

        // Run test and verify notification saved by ProcessingAuditAccessor

        JobNotificationProcessor jobNotificationProcessor = new JobNotificationProcessor(notificationDetailExtractionDelegator, notificationContentProcessor, providerMessageDistributor, List.of(lifecycleCaches));
        jobNotificationProcessor.processNotificationForJob(processedNotificationDetails, ProcessingType.DEFAULT, List.of(notificationModel));

        Set<Long> auditNotificationIds = processingAuditAccessor.getNotificationIds(uuid);

        Mockito.verify(eventManager, Mockito.times(1)).sendEvent(Mockito.any());
        assertEquals(1, auditNotificationIds.size());
        assertTrue(auditNotificationIds.contains(notificationId));
    }

    // Helper Methods

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
        ProjectVersionComponentVersionView projectVersionComponentVersionView = createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.eq(ProjectVersionComponentVersionView.class))).thenReturn(projectVersionComponentVersionView);

        BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

        return new RuleViolationNotificationMessageExtractor(providerKey, servicesFactoryCache, blackDuckPolicyComponentConcernCreator, detailsCreatorFactory, bomComponent404Handler);
    }

    private NotificationExtractorBlackDuckServicesFactoryCache createNotificationExtractorBlackDuckServicesFactoryCache() {
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        AlertProperties properties = new AlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        BlackDuckPropertiesFactory blackDuckPropertiesFactory = new BlackDuckPropertiesFactory(configurationAccessor, GSON, properties, proxyManager);

        return new NotificationExtractorBlackDuckServicesFactoryCache(blackDuckPropertiesFactory);
    }

    private RuleViolationNotificationView createRuleViolationNotificationView(String projectName) {
        RuleViolationNotificationContent notificationContent = new RuleViolationNotificationContent();
        notificationContent.setProjectName(projectName);
        notificationContent.setProjectVersionName("a-project-version");
        notificationContent.setProjectVersion("https://a-project-version");
        notificationContent.setComponentVersionsInViolation(1);

        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPolicy("https://a-policy");
        policyInfo.setPolicyName("a policy");
        policyInfo.setSeverity(PolicyRuleSeverityType.MAJOR.name());
        notificationContent.setPolicyInfos(List.of(policyInfo));

        ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.setBomComponent("https://bom-component");
        componentVersionStatus.setComponentName("component name");
        componentVersionStatus.setComponent("https://component");
        componentVersionStatus.setComponentVersionName("component-version name");
        componentVersionStatus.setComponentVersion("https://component-version");
        componentVersionStatus.setPolicies(List.of(policyInfo.getPolicy()));
        componentVersionStatus.setBomComponentVersionPolicyStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION.name());
        componentVersionStatus.setComponentIssueLink("https://component-issues");
        notificationContent.setComponentVersionStatuses(List.of(componentVersionStatus));

        RuleViolationNotificationView notificationView = new RuleViolationNotificationView();
        notificationView.setContent(notificationContent);
        notificationView.setType(NotificationType.RULE_VIOLATION);

        return notificationView;
    }

    private AlertNotificationModel createNotification(String notificationType, String notificationContent) {
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

    private ProjectVersionComponentVersionView createProjectVersionComponentVersionView() throws IntegrationException {
        ProjectVersionComponentVersionView projectVersionComponentVersionView = new ProjectVersionComponentVersionView();

        projectVersionComponentVersionView.setComponentName(COMPONENT.getValue());
        projectVersionComponentVersionView.setComponentVersion(COMPONENT_VERSION_URL);
        projectVersionComponentVersionView.setComponentVersionName(COMPONENT_VERSION.getValue());
        projectVersionComponentVersionView.setPolicyStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        projectVersionComponentVersionView.setUsages(List.of(UsageType.DYNAMICALLY_LINKED));

        ProjectVersionComponentVersionLicensesView projectVersionComponentVersionLicensesView = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentVersionLicensesView.setLicense("http://licenseLink");
        projectVersionComponentVersionLicensesView.setLicenseDisplay(LICENSE_DISPLAY);
        projectVersionComponentVersionView.setLicenses(List.of(projectVersionComponentVersionLicensesView));

        ResourceLink resourceLink = new ResourceLink();
        resourceLink.setHref(new HttpUrl("https://someHref"));
        resourceLink.setRel("policy-rules");
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrl"));
        meta.setLinks(List.of(resourceLink));
        projectVersionComponentVersionView.setMeta(meta);

        return projectVersionComponentVersionView;
    }
}
