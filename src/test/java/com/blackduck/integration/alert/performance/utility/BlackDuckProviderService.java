/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance.utility;

import static com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigKeys.KEYS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.common.rest.model.MultiFieldModel;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.bdio.model.Forge;
import com.blackduck.integration.bdio.model.externalid.ExternalId;
import com.blackduck.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.blackduck.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.blackduck.integration.blackduck.api.generated.response.ComponentsView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.api.manual.temporary.component.ProjectRequest;
import com.blackduck.integration.blackduck.api.manual.temporary.component.ProjectVersionRequest;
import com.blackduck.integration.blackduck.api.manual.temporary.enumeration.ProjectVersionPhaseType;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfig;
import com.blackduck.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.dataservice.ComponentService;
import com.blackduck.integration.blackduck.service.dataservice.PolicyRuleService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectBomService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class BlackDuckProviderService {
    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));

    private final AlertRequestUtility alertRequestUtility;
    private final BlackDuckServicesFactory blackDuckServicesFactory;
    private final Gson gson;

    private final String blackDuckProviderKey;
    private final String blackDuckProviderUrl;
    private final String blackDuckApiToken;
    private final String blackDuckTimeout;
    private final String blackDuckProviderUniqueName;
    private final String blackDuckProjectName;
    private final String blackDuckProjectVersion;

    public BlackDuckProviderService(AlertRequestUtility alertRequestUtility, Gson gson) {
        this.alertRequestUtility = alertRequestUtility;
        this.gson = gson;

        this.blackDuckProviderKey = new BlackDuckProviderKey().getUniversalKey();

        TestProperties testProperties = new TestProperties();
        this.blackDuckProviderUrl = testProperties.getBlackDuckURL();
        this.blackDuckApiToken = testProperties.getBlackDuckAPIToken();
        this.blackDuckTimeout = testProperties.getOptionalProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT).orElse("300");
        this.blackDuckProviderUniqueName = blackDuckProviderUrl + UUID.randomUUID();
        this.blackDuckProjectName = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PROJECT_NAME);
        this.blackDuckProjectVersion = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PROJECT_VERSION);

        this.blackDuckServicesFactory = setupBlackDuckServicesFactory();
    }

    public static Supplier<ExternalId> getDefaultExternalIdSupplier() {
        return () -> {
            ExternalId commonsFileUploadExternalId = new ExternalId(Forge.MAVEN);
            commonsFileUploadExternalId.setGroup("commons-fileupload");
            commonsFileUploadExternalId.setName("commons-fileupload");
            commonsFileUploadExternalId.setVersion("1.2.1");
            return commonsFileUploadExternalId;
        };
    }

    public static Predicate<ProjectVersionComponentVersionView> getDefaultBomComponentFilter() {
        return (component) -> component.getComponentName().equals("Apache Commons FileUpload") && component.getComponentVersionName().equals("1.2.1");
    }

    public void triggerBlackDuckNotification() throws IntegrationException {
        triggerBlackDuckNotification(getDefaultExternalIdSupplier(), getDefaultBomComponentFilter());
    }

    public void triggerBlackDuckNotification(Supplier<ExternalId> externalIdSupplier, Predicate<ProjectVersionComponentVersionView> componentFilter) throws IntegrationException {
        setupBlackDuckServicesFactory();
        BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckApiClient();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        ProjectVersionWrapper projectVersion = projectService.getProjectVersion(blackDuckProjectName, blackDuckProjectVersion)
            .orElseThrow(() -> new IntegrationException(String.format("Could not find the Black Duck project '%s' version '%s'", blackDuckProjectName, blackDuckProjectVersion)));

        ProjectVersionView projectVersionView = projectVersion.getProjectVersionView();
        List<ProjectVersionComponentVersionView> bomComponents = blackDuckService.getAllResponses(projectVersionView.metaComponentsLink());
        Optional<ProjectVersionComponentVersionView> apacheCommonsFileUpload = bomComponents.stream()
            .filter(componentFilter)
            .findFirst();
        if (apacheCommonsFileUpload.isPresent()) {
            blackDuckService.delete(apacheCommonsFileUpload.get());
            //Thread.currentThread().wait(1000);
        }

        ExternalId externalId = externalIdSupplier.get();

        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        projectBomService.addComponentToProjectVersion(externalId, projectVersionView);
    }

    public void triggerBlackDuckNotificationForProjectVersion(
        ProjectVersionView projectVersionView,
        Supplier<ExternalId> externalIdSupplier,
        Predicate<ProjectVersionComponentVersionView> componentFilter
    ) throws IntegrationException {
        setupBlackDuckServicesFactory();
        BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckApiClient();

        List<ProjectVersionComponentVersionView> bomComponents = blackDuckService.getAllResponses(projectVersionView.metaComponentsLink());
        Optional<ProjectVersionComponentVersionView> apacheCommonsFileUpload = bomComponents.stream()
            .filter(componentFilter)
            .findFirst();
        if (apacheCommonsFileUpload.isPresent()) {
            blackDuckService.delete(apacheCommonsFileUpload.get());
            //Thread.currentThread().wait(1000);
        }

        ExternalId externalId = externalIdSupplier.get();

        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        projectBomService.addComponentToProjectVersion(externalId, projectVersionView);
    }

    public PolicyRuleView createBlackDuckPolicyRuleView(String policyName, Supplier<ExternalId> externalIdSupplier) throws IntegrationException {
        setupBlackDuckServicesFactory();
        ComponentService componentService = blackDuckServicesFactory.createComponentService();

        ExternalId externalId = externalIdSupplier.get();
        ComponentsView searchResult = componentService.getSingleOrEmptyResult(externalId)
            .orElseThrow(() -> new IntegrationException(String.format("Could not find the ComponentsView for component: %s", externalId.getName())));
        ComponentVersionView componentVersionView = componentService.getComponentVersionView(searchResult)
            .orElseThrow(() -> new IntegrationException(String.format("Could not find the ComponentVersionView for component: %s", searchResult.getComponentName())));

        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addComponentVersionCondition(PolicyRuleConditionOperatorType.EQ, componentVersionView);
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(policyName);
        policyRuleView.setEnabled(true);
        policyRuleView.setOverridable(true);
        policyRuleView.setExpression(expressionSet);

        return policyRuleView;
    }

    public void deleteExistingBlackDuckPolicy(PolicyRuleView policyRuleView) throws IntegrationException {
        setupBlackDuckServicesFactory();
        PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();

        policyRuleService.getPolicyRuleViewByName(policyRuleView.getName());
        Optional<PolicyRuleView> policyRuleViewOptional = policyRuleService.getPolicyRuleViewByName(policyRuleView.getName());
        if (policyRuleViewOptional.isPresent()) {
            PolicyRuleView notificationPolicy = policyRuleViewOptional.get();
            intLogger.info(String.format("Policy: %s already exists. Deleting the existing policy.", notificationPolicy.getName()));
            BlackDuckApiClient blackDuckService = blackDuckServicesFactory.getBlackDuckApiClient();
            blackDuckService.delete(notificationPolicy);
        }
    }

    public void triggerBlackDuckPolicyNotification(String policyName, Supplier<ExternalId> externalIdSupplier) throws IntegrationException {
        PolicyRuleView policyRuleView = createBlackDuckPolicyRuleView(policyName, externalIdSupplier);
        deleteExistingBlackDuckPolicy(policyRuleView);

        PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();

        intLogger.info(String.format("Creating policy with the name: %s", policyRuleView.getName()));
        policyRuleService.createPolicyRule(policyRuleView);
    }

    public ProjectVersionWrapper findOrCreateBlackDuckProjectAndVersion(String projectName, String projectVersionName) throws IntegrationException {
        setupBlackDuckServicesFactory();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(projectName);

        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setVersionName(projectVersionName);
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.OPENSOURCE);

        projectRequest.setVersionRequest(projectVersionRequest);

        Optional<ProjectVersionWrapper> existingProjectVersion = projectService.getProjectVersion(projectRequest.getName(), projectVersionRequest.getVersionName());
        if (existingProjectVersion.isPresent()) {
            intLogger.info(String.format("Project: %s Version %s already exists", projectName, projectVersionName));
            return existingProjectVersion.get();
        }
        intLogger.info(String.format("Creating project: %s with version: %s", projectName, projectVersionName));
        return projectService.createProject(projectRequest);
    }

    public void deleteBlackDuckProjectAndVersion(String projectName, String projectVersionName) throws IntegrationException {
        setupBlackDuckServicesFactory();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();

        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(projectName);

        ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
        projectVersionRequest.setVersionName(projectVersionName);
        projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
        projectVersionRequest.setDistribution(ProjectVersionDistributionType.OPENSOURCE);

        projectRequest.setVersionRequest(projectVersionRequest);

        Optional<ProjectVersionWrapper> existingProjectVersion = projectService.getProjectVersion(projectRequest.getName(), projectVersionRequest.getVersionName());
        if (existingProjectVersion.isPresent()) {
            intLogger.info(String.format("Project: %s Version %s already exists", projectName, projectVersionName));
            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
            blackDuckApiClient.delete(existingProjectVersion.get().getProjectVersionView());
            blackDuckApiClient.delete(existingProjectVersion.get().getProjectView());
            intLogger.info(String.format("Deleting project: %s", projectName));
        }
    }

    public String setupBlackDuck() {
        try {
            return findBlackDuckProvider();
        } catch (IntegrationException exception) {
            intLogger.error(exception.getMessage());
            return createBlackDuckProvider();
        }
    }

    public String findBlackDuckProvider() throws IntegrationException {
        String blackDuckProviderSearch = String.format("/api/configuration?context=%s&descriptorName=%s", ConfigContextEnum.GLOBAL, blackDuckProviderKey);
        String response = alertRequestUtility.executeGetRequest(blackDuckProviderSearch, "Could not find the Black Duck provider.");

        MultiFieldModel blackDuckConfigurations = gson.fromJson(response, MultiFieldModel.class);
        FieldModel blackDuckProviderConfiguration = blackDuckConfigurations.getFieldModels().stream()
            .filter(blackDuckConfiguration -> blackDuckConfiguration.getFieldValue("blackduck.url").filter(blackDuckProviderUrl::equals).isPresent())
            .findFirst()
            .orElseThrow(() -> new IntegrationException("Could not find the BlackDuck provider configuration."));

        String blackDuckProviderID = blackDuckProviderConfiguration.getId();
        String blackDuckConfigBody = gson.toJson(blackDuckProviderConfiguration);
        alertRequestUtility.executePutRequest("/api/configuration/" + blackDuckProviderID, blackDuckConfigBody, "Could not save the Black Duck provider.");
        intLogger.info(String.format("Retrieved the Black Duck provider, ID %s.", blackDuckProviderID));

        return blackDuckProviderID;
    }

    private String createBlackDuckProvider() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put("provider.common.config.enabled", new FieldValueModel(List.of("true"), true));
        keyToValues.put("provider.common.config.name", new FieldValueModel(List.of(blackDuckProviderUniqueName), true));
        keyToValues.put("blackduck.url", new FieldValueModel(List.of(blackDuckProviderUrl), true));
        keyToValues.put("blackduck.api.key", new FieldValueModel(List.of(blackDuckApiToken), true));
        keyToValues.put("blackduck.timeout", new FieldValueModel(List.of(blackDuckTimeout), true));
        FieldModel blackDuckProviderConfiguration = new FieldModel(blackDuckProviderKey, ConfigContextEnum.GLOBAL.name(), keyToValues);

        String blackDuckConfigBody = gson.toJson(blackDuckProviderConfiguration);
        try {
            alertRequestUtility.executePostRequest("/api/configuration/validate", blackDuckConfigBody, "Validating the Black Duck provider failed.");
            alertRequestUtility.executePostRequest("/api/configuration/test", blackDuckConfigBody, "Testing the Black Duck provider failed.");
            String creationResponse = alertRequestUtility.executePostRequest("/api/configuration", blackDuckConfigBody, "Could not create the Black Duck provider.");

            JsonObject jsonObject = gson.fromJson(creationResponse, JsonObject.class);
            String blackDuckProviderID = jsonObject.get("id").getAsString();
            intLogger.info(String.format("Configured the Black Duck provider, ID %s.", blackDuckProviderID));
            return blackDuckProviderID;
        } catch (IntegrationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private BlackDuckServicesFactory setupBlackDuckServicesFactory() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder(KEYS.apiToken);
        blackDuckServerConfigBuilder.setUrl(blackDuckProviderUrl)
            .setApiToken(blackDuckApiToken)
            .setTimeoutInSeconds(blackDuckTimeout)
            .setTrustCert(true);
        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        return blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
    }

    public String getBlackDuckProviderKey() {
        return blackDuckProviderKey;
    }

    public String getBlackDuckProviderUrl() {
        return blackDuckProviderUrl;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public String getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public String getBlackDuckProviderUniqueName() {
        return blackDuckProviderUniqueName;
    }

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    public String getBlackDuckProjectVersion() {
        return blackDuckProjectVersion;
    }

    public BlackDuckServicesFactory getBlackDuckServicesFactory() {
        return blackDuckServicesFactory;
    }
}
