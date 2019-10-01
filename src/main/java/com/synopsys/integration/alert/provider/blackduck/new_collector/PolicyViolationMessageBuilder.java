package com.synopsys.integration.alert.provider.blackduck.new_collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.alert.provider.blackduck.new_collector.util.BlackDuckResponseCache;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.component.RemediatingVersionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.response.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomPolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

@Component
public class PolicyViolationMessageBuilder implements BlackDuckMessageBuilder<RuleViolationNotificationView> {
    public static final String CATEGORY_TYPE = "Policy";
    public static final String VULNERABILITY_CHECK_TEXT = "vuln";
    private final Logger logger = LoggerFactory.getLogger(PolicyViolationMessageBuilder.class);
    private final Map<String, ComponentItemPriority> policyPriorityMap = new HashMap<>();

    public PolicyViolationMessageBuilder() {
        policyPriorityMap.put("blocker", ComponentItemPriority.HIGHEST);
        policyPriorityMap.put("critical", ComponentItemPriority.HIGH);
        policyPriorityMap.put("major", ComponentItemPriority.MEDIUM);
        policyPriorityMap.put("minor", ComponentItemPriority.LOW);
        policyPriorityMap.put("trivial", ComponentItemPriority.LOWEST);
        policyPriorityMap.put("unspecified", ComponentItemPriority.NONE);
    }

    @Override
    public String getNotificationType() {
        return NotificationType.RULE_VIOLATION.name();
    }

    @Override
    public List<ProviderMessageContent> buildMessageContents(final Long notificationId, final Date providerCreationDate, final ConfigurationJobModel job, final RuleViolationNotificationView notificationView,
        final BlackDuckBucket blackDuckBucket,
        final BlackDuckServicesFactory blackDuckServicesFactory) {
        long timeout = blackDuckServicesFactory.getBlackDuckHttpClient().getTimeoutInSeconds();
        BlackDuckBucketService bucketService = blackDuckServicesFactory.createBlackDuckBucketService();
        BlackDuckResponseCache responseCache = new BlackDuckResponseCache(bucketService, blackDuckBucket, timeout);
        RuleViolationNotificationContent violationContent = notificationView.getContent();
        ItemOperation operation = ItemOperation.ADD;
        try {
            ProviderMessageContent.Builder projectVersionMessageBuilder = new ProviderMessageContent.Builder()
                                                                              .applyProvider(getProviderName(), blackDuckServicesFactory.getBlackDuckHttpClient().getBaseUrl())
                                                                              .applyTopic("Project", violationContent.getProjectName())
                                                                              .applySubTopic("Project Version", violationContent.getProjectVersionName(), violationContent.getProjectVersion())
                                                                              .applyAction(operation)
                                                                              .applyNotificationId(notificationId)
                                                                              .applyProviderCreationTime(providerCreationDate);
            Map<String, PolicyInfo> policyUrlToInfoMap = violationContent.getPolicyInfos().stream().collect(Collectors.toMap(PolicyInfo::getPolicy, Function.identity()));
            SetMap<ComponentVersionStatus, PolicyInfo> componentPolicies = createComponentToPolicyMapping(violationContent.getComponentVersionStatuses(), policyUrlToInfoMap);
            List<ComponentItem> items = new LinkedList<>();
            for (Map.Entry<ComponentVersionStatus, Set<PolicyInfo>> componentToPolicyEntry : componentPolicies.entrySet()) {
                ComponentVersionStatus componentVersionStatus = componentToPolicyEntry.getKey();
                Set<PolicyInfo> policies = componentToPolicyEntry.getValue();
                final List<ComponentItem> componentItems = retrievePolicyItems(responseCache, componentVersionStatus, policies, notificationId, operation);
                items.addAll(componentItems);
            }
            projectVersionMessageBuilder.applyAllComponentItems(items);
            return List.of(projectVersionMessageBuilder.build());
        } catch (AlertException ex) {
            logger.error("Error creating policy violation message.", ex);
        }

        return List.of();
    }

    private List<ComponentItem> retrievePolicyItems(BlackDuckResponseCache blackDuckResponseCache, ComponentVersionStatus componentVersionStatus, Set<PolicyInfo> policies, Long notificationId, ItemOperation operation) {
        List<ComponentItem> componentItems = new LinkedList<>();
        for (PolicyInfo policyInfo : policies) {
            ComponentItemPriority priority = getPolicyPriority(policyInfo.getSeverity());

            String bomComponentUrl = null;
            if (null != componentVersionStatus) {
                bomComponentUrl = componentVersionStatus.getBomComponent();
            }

            Optional<VersionBomComponentView> optionalBomComponent = getBomComponentView(blackDuckResponseCache, bomComponentUrl);

            List<LinkableItem> policyAttributes = new ArrayList<>();
            //            LinkableItem policyNameItem = createPolicyNameItem(policyInfo);
            //            LinkableItem nullablePolicySeverityItem = createPolicySeverityItem(policyInfo).orElse(null);
            //            optionalBomComponent.ifPresent(bomComponent -> {
            //                policyAttributes.addAll(getLicenseLinkableItems(bomComponent));
            //                policyAttributes.addAll(getUsageLinkableItems(bomComponent));
            //            });
            //
            //            LinkableItem componentItem = new LinkableItem("Component", componentVersionStatus.getComponentName(), componentVersionStatus.getComponent());
            //            Optional<LinkableItem> optionalComponentVersionItem = new LinkableItem("Component Version", componentVersionStatus.getComponentVersionName(), componentVersionStatus.getComponentVersion());
            //
            //            addApplicableItems(operation, priority, componentItem, optionalComponentVersionItem.orElse(null), policyNameItem, nullablePolicySeverityItem, policyAttributes, notificationId)
            //                .ifPresent(componentItems::add);
            //
            //            Optional<PolicyRuleView> optionalPolicyRule = getPolicyRule(policyInfo);
            //            if (optionalPolicyRule.isPresent() && hasVulnerabilityRule(optionalPolicyRule.get())) {
            //                if (optionalBomComponent.isPresent()) {
            //                    List<ComponentItem> vulnerabilityPolicyItems =
            //                        createVulnerabilityPolicyItems(optionalBomComponent.get(), policyNameItem, nullablePolicySeverityItem, componentItem, optionalComponentVersionItem, notificationId);
            //                    componentItems.addAll(vulnerabilityPolicyItems);
            //                }
            //            }
        }
        return componentItems;
    }

    public boolean hasVulnerabilityRule(VersionBomPolicyRuleView policyRule) {
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVulnerabilityRule(PolicyRuleView policyRule) {
        PolicyRuleExpressionSetView expression = policyRule.getExpression();
        List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().toLowerCase().contains(VULNERABILITY_CHECK_TEXT)) {
                return true;
            }
        }
        return false;
    }

    public Optional<PolicyRuleView> getPolicyRule(BlackDuckResponseCache blackDuckResponseCache, PolicyInfo policyInfo) {
        try {
            String policyUrl = policyInfo.getPolicy();
            if (StringUtils.isNotBlank(policyUrl)) {
                return blackDuckResponseCache.getItem(PolicyRuleView.class, policyUrl);
            }
        } catch (Exception e) {
            logger.debug("Unable to get policy rule: {}", policyInfo.getPolicyName());
            logger.debug("Cause:", e);
        }
        return Optional.empty();
    }

    protected ComponentItemPriority getPolicyPriority(String severity) {
        if (StringUtils.isNotBlank(severity)) {
            String severityKey = severity.trim().toLowerCase();
            return policyPriorityMap.getOrDefault(severityKey, ComponentItemPriority.NONE);
        }
        return ComponentItemPriority.NONE;
    }

    public Optional<VersionBomComponentView> getBomComponentView(BlackDuckResponseCache blackDuckResponseCache, String bomComponentUrl) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(bomComponentUrl)) {
            return blackDuckResponseCache.getItem(VersionBomComponentView.class, bomComponentUrl);
        }
        return Optional.empty();
    }

    public List<LinkableItem> getLicenseLinkableItems(VersionBomComponentView bomComponentView) {
        return bomComponentView.getLicenses()
                   .stream()
                   .map(licenseView -> new LinkableItem(BlackDuckContent.LABEL_COMPONENT_LICENSE, licenseView.getLicenseDisplay()))
                   .collect(Collectors.toList());
    }

    public List<LinkableItem> getUsageLinkableItems(VersionBomComponentView bomComponentView) {
        return bomComponentView.getUsages()
                   .stream()
                   .map(MatchedFileUsagesType::prettyPrint)
                   .map(usage -> new LinkableItem(BlackDuckContent.LABEL_COMPONENT_USAGE, usage))
                   .collect(Collectors.toList());
    }

    protected LinkableItem getSeverity(BlackDuckResponseCache blackDuckResponseCache, String vulnerabilityUrl) {
        LinkableItem severityItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITY_SEVERITY, "UNKNOWN");
        try {
            Optional<VulnerabilityView> vulnerabilityView = blackDuckResponseCache.getItem(VulnerabilityView.class, vulnerabilityUrl);
            if (vulnerabilityView.isPresent()) {
                VulnerabilityView vulnerability = vulnerabilityView.get();
                String severity = vulnerability.getSeverity();
                Optional<String> cvss3Severity = getCvss3Severity(vulnerability);
                if (cvss3Severity.isPresent()) {
                    severity = cvss3Severity.get();
                }
                severityItem = new LinkableItem(BlackDuckContent.LABEL_VULNERABILITY_SEVERITY, severity);
            }
        } catch (Exception e) {
            logger.debug("Error fetching vulnerability view", e);
        }

        return severityItem;
    }

    // TODO update this code with an Object from blackduck-common-api when available
    private Optional<String> getCvss3Severity(VulnerabilityView vulnerabilityView) {
        Boolean useCvss3 = vulnerabilityView.getUseCvss3();
        if (null != useCvss3 && useCvss3) {
            JsonObject vulnJsonObject = vulnerabilityView.getJsonElement().getAsJsonObject();
            JsonElement cvss3 = vulnJsonObject.get("cvss3");
            if (null != cvss3) {
                JsonElement cvss3Severity = cvss3.getAsJsonObject().get("severity");
                if (null != cvss3Severity) {
                    return Optional.of(cvss3Severity.getAsString());
                }
            }
        }
        return Optional.empty();
    }

    private SetMap<ComponentVersionStatus, PolicyInfo> createComponentToPolicyMapping(
        Collection<ComponentVersionStatus> componentVersionStatuses, Map<String, PolicyInfo> policyItems) {
        SetMap<ComponentVersionStatus, PolicyInfo> componentToPolicyMapping = SetMap.createDefault();
        for (ComponentVersionStatus componentVersionStatus : componentVersionStatuses) {
            Set<PolicyInfo> componentPolicies = getPoliciesForComponent(componentVersionStatus, policyItems);
            componentToPolicyMapping.addAll(componentVersionStatus, componentPolicies);
        }
        return componentToPolicyMapping;
    }

    private Set<PolicyInfo> getPoliciesForComponent(ComponentVersionStatus componentVersionStatus, Map<String, PolicyInfo> policyItems) {
        return componentVersionStatus.getPolicies().stream()
                   .filter(policyItems::containsKey)
                   .map(policyItems::get)
                   .collect(Collectors.toSet());
    }

    private List<ComponentItem> createVulnerabilityPolicyItems(BlackDuckResponseCache blackDuckResponseCache, BlackDuckService blackDuckService, ComponentService componentService, VersionBomComponentView bomComponent,
        LinkableItem policyNameItem, LinkableItem policySeverity,
        LinkableItem componentItem, Optional<LinkableItem> optionalComponentVersionItem, Long notificationId) {
        List<ComponentItem> vulnerabilityPolicyItems = new ArrayList<>();
        Optional<ProjectVersionWrapper> optionalProjectVersionWrapper = getProjectVersionWrapper(blackDuckResponseCache, bomComponent);
        if (optionalProjectVersionWrapper.isPresent()) {
            try {
                List<VulnerableComponentView> vulnerableComponentViews = getVulnerableComponentViews(blackDuckService, optionalProjectVersionWrapper.get(), bomComponent);
                //                List<ComponentItem> vulnerabilityComponentItems =
                //                    createVulnerabilityPolicyComponentItems(vulnerableComponentViews, policyNameItem, policySeverity, componentItem, optionalComponentVersionItem, notificationId);
                //                vulnerabilityPolicyItems.addAll(vulnerabilityComponentItems);

                // TODO: remove the orElse null.
                ComponentVersionView componentVersionView = blackDuckResponseCache.getItem(ComponentVersionView.class, bomComponent.getComponentVersion()).orElse(null);

                Optional<ComponentItem> remediationComponentItem = createRemediationComponentItem(CATEGORY_TYPE, componentService, componentVersionView, componentItem, optionalComponentVersionItem, policyNameItem, policySeverity, true,
                    notificationId);
                remediationComponentItem.ifPresent(vulnerabilityPolicyItems::add);
            } catch (IntegrationException e) {
                logger.debug("Could not get the project/version. Skipping vulnerability info for this policy: {}. Exception: {}", policyNameItem, e);
            }
        }
        return vulnerabilityPolicyItems;
    }

    public Optional<ProjectVersionWrapper> getProjectVersionWrapper(BlackDuckResponseCache blackDuckResponseCache, VersionBomComponentView versionBomComponent) {
        // TODO Stop using this when Black Duck supports going back to the project-version
        final Optional<String> versionBomComponentHref = versionBomComponent.getHref();
        if (versionBomComponentHref.isPresent()) {
            String versionHref = versionBomComponentHref.get();
            int componentsIndex = versionHref.indexOf(ProjectVersionView.COMPONENTS_LINK);
            String projectVersionUri = versionHref.substring(0, componentsIndex - 1);

            Optional<ProjectVersionView> projectVersion = blackDuckResponseCache.getItem(ProjectVersionView.class, projectVersionUri);
            ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
            projectVersion.ifPresent(wrapper::setProjectVersionView);
            projectVersion.flatMap(version -> blackDuckResponseCache.getItem(ProjectView.class, version.getFirstLink(ProjectVersionView.PROJECT_LINK).orElse("")))
                .ifPresent(wrapper::setProjectView);
            return Optional.of(wrapper);

        }

        return Optional.empty();
    }

    public List<VulnerableComponentView> getVulnerableComponentViews(BlackDuckService blackDuckService, ProjectVersionWrapper projectVersionWrapper, VersionBomComponentView versionBomComponent) throws IntegrationException {
        return blackDuckService.getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE).stream()
                   .filter(vulnerableComponentView -> vulnerableComponentView.getComponentName().equals(versionBomComponent.getComponentName()))
                   .filter(vulnerableComponentView -> vulnerableComponentView.getComponentVersionName().equals(versionBomComponent.getComponentVersionName()))
                   .collect(Collectors.toList());
    }

    protected Optional<ComponentItem> createRemediationComponentItem(String categoryType, ComponentService componentService, ComponentVersionView componentVersionView, LinkableItem componentItem, Optional<LinkableItem> componentVersionItem,
        LinkableItem categoryItem, LinkableItem categoryGrouping, boolean collapseOnCategory, Long notificationId) {
        try {
            List<LinkableItem> remediationItems = getRemediationItems(componentService, componentVersionView);
            if (!remediationItems.isEmpty()) {
                ComponentItem.Builder remediationComponent = new ComponentItem.Builder()
                                                                 .applyCategory(categoryType)
                                                                 .applyOperation(ItemOperation.INFO)
                                                                 .applyPriority(ComponentItemPriority.NONE)
                                                                 .applyComponentData(componentItem)
                                                                 .applyCategoryItem(categoryItem)
                                                                 .applyCategoryGroupingAttribute(categoryGrouping)
                                                                 .applyCollapseOnCategory(collapseOnCategory)
                                                                 .applyAllComponentAttributes(remediationItems)
                                                                 .applyNotificationId(notificationId);
                componentVersionItem.ifPresent(remediationComponent::applySubComponent);

                return Optional.of(remediationComponent.build());
            }
        } catch (IntegrationException e) {
            logger.debug("Could not create remediation component", e);
        }
        return Optional.empty();
    }

    public List<LinkableItem> getRemediationItems(ComponentService componentService, ComponentVersionView componentVersionView) throws IntegrationException {
        List<LinkableItem> remediationItems = new LinkedList<>();
        Optional<RemediationOptionsView> optionalRemediation = componentService.getRemediationInformation(componentVersionView);
        if (optionalRemediation.isPresent()) {
            RemediationOptionsView remediationOptions = optionalRemediation.get();
            createRemediationItem(remediationOptions::getFixesPreviousVulnerabilities, BlackDuckContent.LABEL_REMEDIATION_FIX_PREVIOUS).ifPresent(remediationItems::add);
            createRemediationItem(remediationOptions::getLatestAfterCurrent, BlackDuckContent.LABEL_REMEDIATION_LATEST).ifPresent(remediationItems::add);
            createRemediationItem(remediationOptions::getNoVulnerabilities, BlackDuckContent.LABEL_REMEDIATION_CLEAN).ifPresent(remediationItems::add);
        }
        return remediationItems;
    }

    private Optional<LinkableItem> createRemediationItem(Supplier<RemediatingVersionView> getRemediationOption, String remediationLabel) {
        RemediatingVersionView remediatingVersionView = getRemediationOption.get();
        if (null != remediatingVersionView) {
            String versionText = createRemediationVersionText(remediatingVersionView);
            return Optional.of(new LinkableItem(remediationLabel, versionText, remediatingVersionView.getComponentVersion()));
        }
        return Optional.empty();
    }

    private String createRemediationVersionText(RemediatingVersionView remediatingVersionView) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(remediatingVersionView.getName());
        if (remediatingVersionView.getVulnerabilityCount() != null && remediatingVersionView.getVulnerabilityCount() > 0) {
            stringBuilder.append(" (Vulnerability Count: ");
            stringBuilder.append(remediatingVersionView.getVulnerabilityCount());
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }
}
