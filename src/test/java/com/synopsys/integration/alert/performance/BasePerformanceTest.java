package com.synopsys.integration.alert.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;
import com.synopsys.integration.alert.performance.model.BlackDuckPerformanceProperties;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Tag(TestTags.DEFAULT_PERFORMANCE)
public abstract class BasePerformanceTest {
    public static final String ROLE_ALERT_ADMIN = "ALERT_ADMIN";
    protected final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));

    private final BlackDuckPerformanceProperties blackDuckProperties = new BlackDuckPerformanceProperties();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final String localAlertURL = "http://localhost/";

    private AlertRequestUtility alertRequestUtility;
    private BlackDuckServicesFactory blackDuckServicesFactory = null;

    public abstract AlertRequestUtility createAlertRequestUtility();

    @BeforeEach
    public void setupTests() {
        alertRequestUtility = createAlertRequestUtility();
    }

    public void triggerBlackDuckNotification() throws Exception {
        setupBlackDuckServicesFactory();
        BlackDuckService blackDuckService = blackDuckServicesFactory.getBlackDuckService();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        String blackDuckProjectName = blackDuckProperties.getBlackDuckProjectName();
        String blackDuckProjectVersion = blackDuckProperties.getBlackDuckProjectVersion();
        ProjectVersionWrapper projectVersion = projectService.getProjectVersion(blackDuckProjectName, blackDuckProjectVersion)
                                                   .orElseThrow(() -> new IntegrationException(String.format("Could not find the Black Duck project '%s' version '%s'", blackDuckProjectName, blackDuckProjectVersion)));

        ProjectVersionView projectVersionView = projectVersion.getProjectVersionView();
        List<ProjectVersionComponentView> bomComponents = blackDuckService.getAllResponses(projectVersionView, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        Optional<ProjectVersionComponentView> apacheCommonsFileUpload = bomComponents.stream()
                                                                            .filter(component -> component.getComponentName().equals("Apache Commons FileUpload"))
                                                                            .filter(component -> component.getComponentVersionName().equals("1.2.1"))
                                                                            .findFirst();
        if (apacheCommonsFileUpload.isPresent()) {
            blackDuckService.delete(apacheCommonsFileUpload.get());
            //Thread.currentThread().wait(1000);
        }

        ExternalId commonsFileUploadExternalId = new ExternalId(Forge.MAVEN);
        commonsFileUploadExternalId.setGroup("commons-fileupload");
        commonsFileUploadExternalId.setName("commons-fileupload");
        commonsFileUploadExternalId.setVersion("1.2.1");

        ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();
        projectBomService.addComponentToProjectVersion(commonsFileUploadExternalId, projectVersionView);
    }

    private void setupBlackDuckServicesFactory() {
        if (null == blackDuckServicesFactory) {
            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
            blackDuckServerConfigBuilder.setUrl(blackDuckProperties.getBlackDuckProviderUrl())
                .setApiToken(blackDuckProperties.getBlackDuckApiToken())
                .setTimeoutInSeconds(blackDuckProperties.getBlackDuckTimeout())
                .setTrustCert(true);
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(intLogger);
        }
    }

    public String setupBlackDuck() throws Exception {
        try {
            return findBlackDuckProvider();
        } catch (IntegrationException exception) {
            intLogger.error(exception.getMessage());
            return createBlackDuckProvider();
        }
    }

    public String findBlackDuckProvider() throws Exception {
        String blackDuckProviderSearch = String.format("api/configuration?context=%s&descriptorName=%s", ConfigContextEnum.GLOBAL, blackDuckProperties.getBlackDuckProviderKey());
        String response = alertRequestUtility.executeGetRequest(blackDuckProviderSearch, "Could not find the Black Duck provider.");

        MultiFieldModel blackDuckConfigurations = gson.fromJson(response, MultiFieldModel.class);
        FieldModel blackDuckProviderConfiguration = blackDuckConfigurations.getFieldModels().stream()
                                                        .filter(blackDuckConfiguration -> blackDuckConfiguration.getFieldValue("blackduck.url").isPresent())
                                                        .filter(blackDuckConfiguration -> blackDuckConfiguration.getFieldValue("blackduck.url").get().equals(blackDuckProperties.getBlackDuckProviderUrl()))
                                                        .findFirst()
                                                        .orElseThrow(() -> new IntegrationException("Could not find the BlackDuck provider configuration."));

        String blackDuckProviderID = blackDuckProviderConfiguration.getId();
        String blackDuckConfigBody = gson.toJson(blackDuckProviderConfiguration);
        alertRequestUtility.executePutRequest("/api/configuration/" + blackDuckProviderID, blackDuckConfigBody, "Could not save the Black Duck provider.");
        intLogger.info(String.format("Retrieved the Black Duck provider, ID %s.", blackDuckProviderID));

        return blackDuckProviderID;
    }

    private String createBlackDuckProvider() throws Exception {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put("provider.common.config.enabled", new FieldValueModel(List.of("true"), true));
        keyToValues.put("provider.common.config.name", new FieldValueModel(List.of(blackDuckProperties.getBlackDuckProviderUniqueName()), true));
        keyToValues.put("blackduck.url", new FieldValueModel(List.of(blackDuckProperties.getBlackDuckProviderUrl()), true));
        keyToValues.put("blackduck.api.key", new FieldValueModel(List.of(blackDuckProperties.getBlackDuckApiToken()), true));
        keyToValues.put("blackduck.timeout", new FieldValueModel(List.of(blackDuckProperties.getBlackDuckTimeout()), true));
        FieldModel blackDuckProviderConfiguration = new FieldModel(blackDuckProperties.getBlackDuckProviderKey(), ConfigContextEnum.GLOBAL.name(), keyToValues);

        String blackDuckConfigBody = gson.toJson(blackDuckProviderConfiguration);

        alertRequestUtility.executePostRequest("/api/configuration/validate", blackDuckConfigBody, "Validating the Black Duck provider failed.");
        alertRequestUtility.executePostRequest("/api/configuration/test", blackDuckConfigBody, "Testing the Black Duck provider failed.");
        String creationResponse = alertRequestUtility.executePostRequest("/api/configuration", blackDuckConfigBody, "Could not create the Black Duck provider.");

        JsonObject jsonObject = gson.fromJson(creationResponse, JsonObject.class);
        String blackDuckProviderID = jsonObject.get("id").getAsString();
        intLogger.info(String.format("Configured the Black Duck provider, ID %s.", blackDuckProviderID));
        return blackDuckProviderID;
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

    public AlertRequestUtility getAlertRequestUtility() {
        return alertRequestUtility;
    }

    public BlackDuckPerformanceProperties getBlackDuckProperties() {
        return blackDuckProperties;
    }

    public Gson getGson() {
        return gson;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public String getLocalAlertURL() {
        return localAlertURL;
    }

    public BlackDuckServicesFactory getBlackDuckServicesFactory() {
        return blackDuckServicesFactory;
    }

}
