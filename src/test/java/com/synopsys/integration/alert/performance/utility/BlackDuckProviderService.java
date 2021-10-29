package com.synopsys.integration.alert.performance.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

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

    public static final Supplier<ExternalId> getDefaultExternalIdSupplier() {
        return () -> {
            ExternalId commonsFileUploadExternalId = new ExternalId(Forge.MAVEN);
            commonsFileUploadExternalId.setGroup("commons-fileupload");
            commonsFileUploadExternalId.setName("commons-fileupload");
            commonsFileUploadExternalId.setVersion("1.2.1");
            return commonsFileUploadExternalId;
        };
    }

    public static final Predicate<ProjectVersionComponentVersionView> getDefaultBomComponentFilter() {
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
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
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
