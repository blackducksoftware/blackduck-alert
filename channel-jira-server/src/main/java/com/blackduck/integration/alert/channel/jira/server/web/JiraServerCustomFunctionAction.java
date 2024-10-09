package com.blackduck.integration.alert.channel.jira.server.web;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.util.JiraPluginCheckUtils;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.CustomFunctionAction;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.HttpServletContentWrapper;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.rest.service.PluginManagerService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.rest.RestConstants;
import com.blackduck.integration.rest.exception.IntegrationRestException;
import com.google.gson.Gson;

/**
 * @deprecated This class is part of the old Alert REST API. It has since been replaced by JiraServerInstallPluginAction and is set for removal in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class JiraServerCustomFunctionAction extends CustomFunctionAction<String> {
    private final Logger logger = LoggerFactory.getLogger(JiraServerCustomFunctionAction.class);

    private final JiraServerGlobalConfigurationFieldModelValidator globalConfigurationValidator;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final Gson gson;
    private final JiraServerGlobalConfigAccessor globalConfigAccessor;

    @Autowired
    public JiraServerCustomFunctionAction(
        AuthorizationManager authorizationManager,
        JiraServerGlobalConfigurationFieldModelValidator globalConfigurationValidator,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        Gson gson,
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor
    ) {
        super(authorizationManager);
        this.globalConfigurationValidator = globalConfigurationValidator;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.gson = gson;
        this.globalConfigAccessor = jiraServerGlobalConfigAccessor;
    }

    @Override
    public ActionResponse<String> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper ignoredServletContent) {
        try {
            Optional<UUID> configurationID = getConfigurationId(fieldModel);
            if (configurationID.isEmpty()) {
                return new ActionResponse<>(
                    HttpStatus.NOT_FOUND,
                    "Jira Server configuration not found. Please include the Jira Server configuration id in the request body."
                );
            }
            JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraProperties(configurationID.get());
            JiraServerServiceFactory jiraServicesFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);
            PluginManagerService jiraAppService = jiraServicesFactory.createPluginManagerService();
            try {
                jiraAppService.installMarketplaceServerApp(JiraConstants.JIRA_APP_KEY);
            } catch (IntegrationRestException e) {
                if (RestConstants.NOT_FOUND_404 == e.getHttpStatusCode()) {
                    return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format(
                        "The marketplace listing of the '%s' app may not support your version of Jira. Please install the app manually or request a compatibility update. Error: %s",
                        JiraConstants.JIRA_ALERT_APP_NAME,
                        e.getMessage()
                    ));
                }
                return createBadRequestIntegrationException(e);
            }
            boolean jiraPluginInstalled = JiraPluginCheckUtils.checkIsAppInstalledAndRetryIfNecessary(jiraAppService);
            if (!jiraPluginInstalled) {
                return new ActionResponse<>(
                    HttpStatus.NOT_FOUND,
                    String.format(
                        "Unable to confirm Jira server successfully installed the '%s' plugin. Please verify the installation on you Jira server.",
                        JiraConstants.JIRA_ALERT_APP_NAME
                    )
                );
            }
            return new ActionResponse<>(HttpStatus.OK, String.format("Successfully installed the '%s' plugin on Jira server.", JiraConstants.JIRA_ALERT_APP_NAME));
        } catch (IntegrationException e) {
            return createBadRequestIntegrationException(e);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating Jira plugin installation.", e);
            Thread.currentThread().interrupt();
            return new ActionResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR,
                String.format("Thread was interrupted while validating Jira '%s' plugin installation: %s", JiraConstants.JIRA_ALERT_APP_NAME, e.getMessage())
            );
        }
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        return globalConfigurationValidator.validate(fieldModel);
    }

    private ActionResponse<String> createBadRequestIntegrationException(IntegrationException error) {
        logger.error("There was an issue connecting to Jira server", error);
        return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The following error occurred when connecting to Jira server: " + error.getMessage());
    }

    private Optional<UUID> getConfigurationId(FieldModel fieldModel) {
        return parseConfigIdFromFieldModel(fieldModel)
            .or(this::readIDFromDatabase);
    }

    private Optional<UUID> parseConfigIdFromFieldModel(FieldModel fieldModel) {
        if (StringUtils.isBlank(fieldModel.getId())) {
            return Optional.empty();
        }
        UUID configId = null;
        try {
            configId = UUID.fromString(fieldModel.getId());
        } catch (IllegalArgumentException ex) {
            logger.error("FieldModel cannot parse id for Jira Server Config UUID from id field.", ex);
        }
        return Optional.ofNullable(configId);
    }

    private Optional<UUID> readIDFromDatabase() {
        return globalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
            .map(JiraServerGlobalConfigModel::getId)
            .map(UUID::fromString);
    }
}
