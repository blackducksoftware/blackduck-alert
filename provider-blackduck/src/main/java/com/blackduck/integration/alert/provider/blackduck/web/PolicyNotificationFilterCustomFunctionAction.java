package com.blackduck.integration.alert.provider.blackduck.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.PagedCustomFunctionAction;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.blackduck.integration.alert.common.rest.HttpServletContentWrapper;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.rest.model.FieldValueModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.http.BlackDuckPageResponse;
import com.blackduck.integration.blackduck.http.BlackDuckQuery;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.blackduck.http.BlackDuckRequestFilter;
import com.blackduck.integration.blackduck.http.client.BlackDuckHttpClient;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;

@Component
public class PolicyNotificationFilterCustomFunctionAction extends PagedCustomFunctionAction<NotificationFilterModelOptions> {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()));
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final ConfigurationFieldModelConverter fieldModelConverter;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    protected PolicyNotificationFilterCustomFunctionAction(
        AuthorizationManager authorizationManager,
        BlackDuckPropertiesFactory blackDuckPropertiesFactory,
        ConfigurationFieldModelConverter fieldModelConverter,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor
    ) {
        super(authorizationManager);
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.fieldModelConverter = fieldModelConverter;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    public ActionResponse<NotificationFilterModelOptions> createPagedActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(ProviderDescriptor.KEY_NOTIFICATION_TYPES);
        Collection<String> selectedNotificationTypes = fieldValueModel.map(FieldValueModel::getValues).orElse(List.of());

        int totalPages = 1;
        List<NotificationFilterModel> options = List.of();

        if (isJobFilterableByPolicy(selectedNotificationTypes)) {
            try {
                Optional<BlackDuckServicesFactory> blackDuckServicesFactory = createBlackDuckServicesFactory(fieldModel);
                if (blackDuckServicesFactory.isPresent()) {
                    BlackDuckPageResponse<PolicyRuleView> policyRulesPage = retrievePolicyRules(blackDuckServicesFactory.get(), pageNumber, pageSize, searchTerm);
                    totalPages = (policyRulesPage.getTotalCount() + (pageSize - 1)) / pageSize;
                    options = convertToNotificationFilterModel(policyRulesPage.getItems());
                }
            } catch (IntegrationException e) {
                logger.errorAndDebug("There was an issue communicating with Black Duck. " + e.getMessage(), e);
                throw new AlertException("Unable to communicate with Black Duck.", e);
            }
        }

        NotificationFilterModelOptions notificationFilterModelOptions = new NotificationFilterModelOptions(totalPages, pageNumber, pageSize, options);
        return new ActionResponse<>(HttpStatus.OK, notificationFilterModelOptions);
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        Optional<String> providerConfigId = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        Optional<String> notificationTypes = fieldModel.getFieldValue(ProviderDescriptor.KEY_NOTIFICATION_TYPES);

        Set<AlertFieldStatus> errors = new HashSet<>();
        if (providerConfigId.isEmpty()) {
            AlertFieldStatus missingProviderConfig = AlertFieldStatus.error(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, String.format("Missing %s", ProviderDescriptor.LABEL_PROVIDER_CONFIG_NAME));
            errors.add(missingProviderConfig);
        }

        if (notificationTypes.isEmpty()) {
            AlertFieldStatus missingNotificationTypes = AlertFieldStatus.error(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, String.format("Missing %s", ProviderDescriptor.KEY_NOTIFICATION_TYPES));
            errors.add(missingNotificationTypes);
        }

        return errors;
    }

    private boolean isJobFilterableByPolicy(Collection<String> notificationTypes) {
        Set<String> filterableNotificationType = Set.of(
            NotificationType.POLICY_OVERRIDE,
            NotificationType.RULE_VIOLATION,
            NotificationType.RULE_VIOLATION_CLEARED
        ).stream().map(NotificationType::name).collect(Collectors.toSet());
        return notificationTypes.stream().anyMatch(filterableNotificationType::contains);
    }

    private BlackDuckPageResponse<PolicyRuleView> retrievePolicyRules(BlackDuckServicesFactory blackDuckServicesFactory, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        ApiDiscovery apiDiscovery = blackDuckServicesFactory.getApiDiscovery();
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        BlackDuckMultipleRequest<PolicyRuleView> spec = new BlackDuckRequestBuilder()
            .commonGet()
            .setLimitAndOffset(pageSize, pageNumber * pageSize)
            .addBlackDuckQuery(new BlackDuckQuery("name", searchTerm))
            .addBlackDuckFilter(BlackDuckRequestFilter.createFilterWithSingleValue("policyRuleEnabled", "true"))
            .buildBlackDuckRequest(apiDiscovery.metaPolicyRulesLink());
        return blackDuckApiClient.getPageResponse(spec);
    }

    private List<NotificationFilterModel> convertToNotificationFilterModel(List<PolicyRuleView> policyRules) {
        return policyRules.stream()
            .map(PolicyRuleView::getName)
            .map(NotificationFilterModel::new)
            .collect(Collectors.toList());
    }

    private Optional<BlackDuckServicesFactory> createBlackDuckServicesFactory(FieldModel fieldModel) throws IntegrationException {
        Optional<BlackDuckProperties> optionalBlackDuckProperties = createBlackDuckProperties(fieldModel);
        if (optionalBlackDuckProperties.isPresent()) {
            BlackDuckProperties blackDuckProperties = optionalBlackDuckProperties.get();
            BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(logger);
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, logger);
            return Optional.of(blackDuckServicesFactory);
        }
        return Optional.empty();
    }

    private Optional<BlackDuckProperties> createBlackDuckProperties(FieldModel fieldModel) {
        FieldUtility fieldUtility = fieldModelConverter.convertToFieldAccessor(fieldModel);
        Long providerConfigId = fieldUtility.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).orElse(null);
        if (null == providerConfigId) {
            return Optional.empty();
        }

        return configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
            .map(blackDuckPropertiesFactory::createProperties);
    }

}
