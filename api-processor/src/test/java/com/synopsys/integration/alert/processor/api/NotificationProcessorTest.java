package com.synopsys.integration.alert.processor.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessage;
import com.synopsys.integration.alert.processor.api.extract.model.ProcessedProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.provider.blackduck.processor.detail.RuleViolationNotificationDetailExtractor;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;

public class NotificationProcessorTest {
    private static final Gson GSON = new GsonBuilder().create();
    private static final BlackDuckResponseResolver BLACK_DUCK_RESPONSE_RESOLVER = new BlackDuckResponseResolver(GSON);
    private static final RuleViolationNotificationDetailExtractor RULE_VIOLATION_NDE = new RuleViolationNotificationDetailExtractor();

    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, new LinkableItem("Black Duck", "bd-server", "https://bd-server"));
    private static final SimpleMessage SIMPLE_MESSAGE = SimpleMessage.original(PROVIDER_DETAILS, "Mock message", "Mock message description", List.of());
    private static final ProcessedProviderMessage<SimpleMessage> PROCESSED_PROVIDER_MESSAGE = new ProcessedProviderMessage<>(Set.of(1L), SIMPLE_MESSAGE);
    private static final ProcessedProviderMessageHolder MESSAGE_HOLDER = new ProcessedProviderMessageHolder(List.of(), List.of(PROCESSED_PROVIDER_MESSAGE));

    @Test
    // Test for when no jobs match within the first page of jobs
    // Passing case: A distribution event is sent
    public void processNotificationsWithMoreThanAPageOfJobsTest() {
        String projectName = "2468 - Test Project";
        String matchingProjectNamePattern = "2468.*";
        String nonMatchingProjectNamePattern = "13579asdf - DO NOT MATCH";

        String targetNotificationType = NotificationType.RULE_VIOLATION.name();
        String nonTargetNotificationType = NotificationType.LICENSE_LIMIT.name();
        RuleViolationNotificationView ruleViolationNotificationView = createRuleViolationNotificationView(projectName);

        String notificationContentString = GSON.toJson(ruleViolationNotificationView);
        AlertNotificationModel notification = createNotification(targetNotificationType, notificationContentString);

        FilteredDistributionJobResponseModel matchingJob = createJob(targetNotificationType, true, matchingProjectNamePattern);
        List<FilteredDistributionJobResponseModel> nonMatchingJobs = createNonMatchingJobs(250, nonTargetNotificationType, true, nonMatchingProjectNamePattern);

        // Only needs to handle the target notification type
        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(BLACK_DUCK_RESPONSE_RESOLVER, List.of(RULE_VIOLATION_NDE));

        ProcessingJobAccessor processingJobAccessor = new MockProcessingJobAccessor(nonMatchingJobs, matchingJob, 199);

        // This is needed to verify the notification is "sent"
        ProviderMessageDistributor distributor = Mockito.mock(ProviderMessageDistributor.class);
        Mockito.doNothing().when(distributor).distribute(Mockito.any(), Mockito.any());

        NotificationProcessor notificationProcessor = createNotificationProcessor(extractionDelegator, processingJobAccessor, distributor);
        notificationProcessor.processNotifications(List.of(notification), List.of(FrequencyType.REAL_TIME, FrequencyType.DAILY));

        // Exactly one distribution event should be sent
        Mockito.verify(distributor, Mockito.times(1)).distribute(Mockito.any(), Mockito.any());
    }

    // ==============
    // Helper methods
    // ==============

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

    private NotificationProcessor createNotificationProcessor(NotificationDetailExtractionDelegator extractionDelegator, ProcessingJobAccessor processingJobAccessor, ProviderMessageDistributor distributor) {
        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(processingJobAccessor);
        return new NotificationProcessor(
            extractionDelegator,
            jobNotificationMapper,
            createNotificationContentProcessor(),
            distributor,
            List.of(),
            createNotificationAccessor()
        );
    }

    private NotificationContentProcessor createNotificationContentProcessor() {
        NotificationContentProcessor notificationContentProcessor = Mockito.mock(NotificationContentProcessor.class);
        Mockito.when(notificationContentProcessor.processNotificationContent(Mockito.any(), Mockito.any())).thenReturn(MESSAGE_HOLDER);
        return notificationContentProcessor;
    }

    private NotificationAccessor createNotificationAccessor() {
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.doNothing().when(notificationAccessor).setNotificationsProcessed(Mockito.anyList());
        return notificationAccessor;
    }

    private List<FilteredDistributionJobResponseModel> createNonMatchingJobs(int numberToCreate, String notificationType, boolean filterByProject, String projectNamePattern) {
        List<FilteredDistributionJobResponseModel> nonMatchingJobs = new ArrayList<>(numberToCreate);
        for (int i = 0; i < numberToCreate; i++) {
            FilteredDistributionJobResponseModel job = createJob(notificationType, filterByProject, projectNamePattern);
            nonMatchingJobs.add(job);
        }
        return nonMatchingJobs;
    }

    private FilteredDistributionJobResponseModel createJob(String notificationType, boolean filterByProject, String projectNamePattern) {
        return new FilteredDistributionJobResponseModel(
            UUID.randomUUID(),
            ProcessingType.DEFAULT,
            "a channel",
            "job name",
            List.of(notificationType),
            List.of(),
            List.of(),
            List.of(),
            filterByProject,
            projectNamePattern,
            null
        );
    }

}
