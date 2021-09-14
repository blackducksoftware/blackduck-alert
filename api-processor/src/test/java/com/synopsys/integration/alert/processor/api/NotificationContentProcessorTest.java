package com.synopsys.integration.alert.processor.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.digest.ProjectMessageDigester;
import com.synopsys.integration.alert.processor.api.extract.ProviderMessageExtractionDelegator;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.processor.api.summarize.ProjectMessageSummarizer;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.RuleViolationNotificationMessageExtractor;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.alert.provider.blackduck.processor.model.RuleViolationUniquePolicyNotificationContent;
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
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class NotificationContentProcessorTest {
    private static final Gson GSON = new GsonBuilder().create();

    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Black Duck", "bd-server", "https://bd-server"));
    private static final LinkableItem COMPONENT = new LinkableItem("Component", "BOM component name");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final String LICENSE_DISPLAY = "licenseDisplay";

    private final ProjectMessageDigester projectMessageDigester = new ProjectMessageDigester();
    private final ProjectMessageSummarizer projectMessageSummarizer = new ProjectMessageSummarizer();
    private final PolicyInfo policyInfo = new PolicyInfo();

    private NotificationContentProcessor notificationContentProcessor;

    private final Long notificationId = 123L;

    @BeforeEach
    public void init() throws IntegrationException {
        policyInfo.setPolicy("https://a-policy");
        policyInfo.setPolicyName("a policy");
        policyInfo.setSeverity(PolicyRuleSeverityType.MAJOR.name());

        RuleViolationNotificationMessageExtractor ruleViolationNotificationMessageExtractor = createRuleViolationNotificationMessageExtractor();
        ProviderMessageExtractionDelegator providerMessageExtractionDelegator = new ProviderMessageExtractionDelegator(List.of(ruleViolationNotificationMessageExtractor));
        notificationContentProcessor = new NotificationContentProcessor(providerMessageExtractionDelegator, projectMessageDigester, projectMessageSummarizer);
    }

    @Test
    public void processNotificationContentDefaultTest() {
        String projectName = "TestProjectName";
        String projectVersionName = "TestProjectVersionName";

        //Create a NotificationContentWrapper
        RuleViolationNotificationView ruleViolationNotificationView = createRuleViolationNotificationView(projectName, projectVersionName);
        String notificationContentString = GSON.toJson(ruleViolationNotificationView);
        AlertNotificationModel notificationModel = createNotification(NotificationType.RULE_VIOLATION.name(), notificationContentString);

        RuleViolationUniquePolicyNotificationContent notificationContent = createRuleViolationUniquePolicyNotificationContent(projectName, projectVersionName);

        NotificationContentWrapper notificationContentWrapper = new NotificationContentWrapper(notificationModel, notificationContent, RuleViolationUniquePolicyNotificationContent.class);

        //Run the test
        ProcessedProviderMessageHolder processedProviderMessageHolder = notificationContentProcessor.processNotificationContent(ProcessingType.DEFAULT, List.of(notificationContentWrapper));
        runProjectMessageAssertions(processedProviderMessageHolder, projectName, projectVersionName);
    }

    @Test
    public void processNotificationContentDigestTest() {
        String projectName = "TestProjectName";
        String projectVersionName = "TestProjectVersionName";

        RuleViolationNotificationView ruleViolationNotificationView = createRuleViolationNotificationView(projectName, projectVersionName);
        String notificationContentString = GSON.toJson(ruleViolationNotificationView);
        AlertNotificationModel notificationModel = createNotification(NotificationType.RULE_VIOLATION.name(), notificationContentString);

        RuleViolationUniquePolicyNotificationContent notificationContent = createRuleViolationUniquePolicyNotificationContent(projectName, projectVersionName);

        NotificationContentWrapper notificationContentWrapper1 = new NotificationContentWrapper(notificationModel, notificationContent, RuleViolationUniquePolicyNotificationContent.class);
        NotificationContentWrapper notificationContentWrapper2 = new NotificationContentWrapper(notificationModel, notificationContent, RuleViolationUniquePolicyNotificationContent.class);

        //When set to digest, the NotificationContentProcessor will combine duplicate duplicate messages created from the two NotificationContentWrappers to a single message
        ProcessedProviderMessageHolder processedProviderMessageHolder = notificationContentProcessor.processNotificationContent(ProcessingType.DIGEST, List.of(notificationContentWrapper1, notificationContentWrapper2));
        runProjectMessageAssertions(processedProviderMessageHolder, projectName, projectVersionName);
    }

    @Test
    public void processNotificationContentSummaryTest() {
        String projectName = "TestProjectName";
        String projectVersionName = "TestProjectVersionName";

        RuleViolationNotificationView ruleViolationNotificationView = createRuleViolationNotificationView(projectName, projectVersionName);
        String notificationContentString = GSON.toJson(ruleViolationNotificationView);
        AlertNotificationModel notificationModel = createNotification(NotificationType.RULE_VIOLATION.name(), notificationContentString);

        RuleViolationUniquePolicyNotificationContent notificationContent = createRuleViolationUniquePolicyNotificationContent(projectName, projectVersionName);

        NotificationContentWrapper notificationContentWrapper1 = new NotificationContentWrapper(notificationModel, notificationContent, RuleViolationUniquePolicyNotificationContent.class);

        //When set to summary, project messages will be summarized into a SimpleMessage rather than ProjectMessage
        ProcessedProviderMessageHolder processedProviderMessageHolder = notificationContentProcessor.processNotificationContent(ProcessingType.SUMMARY, List.of(notificationContentWrapper1));
        List<ProcessedProviderMessage<ProjectMessage>> processedProviderMessages = processedProviderMessageHolder.getProcessedProjectMessages();
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages = processedProviderMessageHolder.getProcessedSimpleMessages();

        assertTrue(processedProviderMessages.isEmpty());
        assertEquals(1, processedSimpleMessages.size());
        ProcessedProviderMessage<SimpleMessage> processedSimpleMessage = processedSimpleMessages.get(0);
        assertEquals(1, processedSimpleMessage.getNotificationIds().size());
        assertTrue(processedSimpleMessage.getNotificationIds().contains(notificationId));
        SimpleMessage simpleMessage = processedSimpleMessage.getProviderMessage();
        assertEquals(PROVIDER_DETAILS, simpleMessage.getProviderDetails());

        assertTrue(simpleMessage.getSource().isPresent());
        ProjectMessage sourceProjectMessage = simpleMessage.getSource().get();
        assertEquals(projectName, sourceProjectMessage.getProject().getValue());
        assertTrue(sourceProjectMessage.getProjectVersion().isPresent());
        assertEquals(projectVersionName, sourceProjectMessage.getProjectVersion().get().getValue());
    }

    private void runProjectMessageAssertions(ProcessedProviderMessageHolder processedProviderMessageHolder, String projectName, String projectVersionName) {
        List<ProcessedProviderMessage<ProjectMessage>> processedProjectMessages = processedProviderMessageHolder.getProcessedProjectMessages();
        List<ProcessedProviderMessage<SimpleMessage>> processedSimpleMessages = processedProviderMessageHolder.getProcessedSimpleMessages();

        assertTrue(processedSimpleMessages.isEmpty());
        assertEquals(1, processedProjectMessages.size());
        ProcessedProviderMessage<ProjectMessage> processedProjectMessage = processedProjectMessages.get(0);
        assertEquals(1, processedProjectMessage.getNotificationIds().size());
        assertTrue(processedProjectMessage.getNotificationIds().contains(notificationId));
        ProjectMessage projectMessage = processedProjectMessage.getProviderMessage();
        assertEquals(projectName, projectMessage.getProject().getValue());
        assertTrue(projectMessage.getProjectVersion().isPresent());
        assertEquals(projectVersionName, projectMessage.getProjectVersion().get().getValue());
        assertEquals(PROVIDER_DETAILS, projectMessage.getProviderDetails());
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
        ProjectVersionComponentVersionView projectVersionComponentVersionView = createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.eq(ProjectVersionComponentVersionView.class))).thenReturn(projectVersionComponentVersionView);

        BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

        return new RuleViolationNotificationMessageExtractor(providerKey, servicesFactoryCache, blackDuckPolicyComponentConcernCreator, detailsCreatorFactory, bomComponent404Handler);
    }

    private RuleViolationNotificationView createRuleViolationNotificationView(String projectName, String projectVersionName) {
        RuleViolationNotificationContent notificationContent = createNotificationContent(projectName, projectVersionName);

        RuleViolationNotificationView notificationView = new RuleViolationNotificationView();
        notificationView.setContent(notificationContent);
        notificationView.setType(NotificationType.RULE_VIOLATION);

        return notificationView;
    }

    private AlertNotificationModel createNotification(String notificationType, String notificationContent) {
        return new AlertNotificationModel(
            notificationId,
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

    private RuleViolationUniquePolicyNotificationContent createRuleViolationUniquePolicyNotificationContent(String projectName, String projectVersionName) {
        int componentVersionsInViolation = 1;
        RuleViolationNotificationContent notificationContent = createNotificationContent(projectName, projectVersionName);
        return new RuleViolationUniquePolicyNotificationContent(
            projectName,
            notificationContent.getProjectVersionName(),
            notificationContent.getProjectVersion(),
            componentVersionsInViolation,
            notificationContent.getComponentVersionStatuses(),
            policyInfo
        );
    }

    private RuleViolationNotificationContent createNotificationContent(String projectName, String projectVersionName) {
        RuleViolationNotificationContent notificationContent = new RuleViolationNotificationContent();
        notificationContent.setProjectName(projectName);
        notificationContent.setProjectVersionName(projectVersionName);
        notificationContent.setProjectVersion("https://a-project-version");
        notificationContent.setComponentVersionsInViolation(1);

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

        return notificationContent;
    }
}
