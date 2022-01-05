/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.PagedCustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.provider.blackduck.validator.BlackDuckApiTokenValidator;

@Component
public class BlackDuckProjectCustomFunctionAction extends PagedCustomFunctionAction<ProviderProjectOptions> {
    private static final String MISSING_PROVIDER_ERROR = "A provider configuration is required to retrieve projects.";

    private final ProviderDataAccessor providerDataAccessor;
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;

    @Autowired
    public BlackDuckProjectCustomFunctionAction(
        AuthorizationManager authorizationManager,
        ProviderDataAccessor providerDataAccessor,
        BlackDuckPropertiesFactory blackDuckPropertiesFactory
    ) {
        super(authorizationManager);
        this.providerDataAccessor = providerDataAccessor;
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
    }

    @Override
    public ActionResponse<ProviderProjectOptions> createPagedActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper, int pageNumber, int pageSize, String searchTerm) {
        String providerName = fieldModel.getFieldValue(ChannelDescriptor.KEY_PROVIDER_TYPE).orElse("");
        if (StringUtils.isBlank(providerName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_PROVIDER_ERROR);
        }

        Long blackDuckConfigId = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
            .map(Long::parseLong)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_PROVIDER_ERROR));

        validateBlackDuckConfiguration(blackDuckConfigId);

        return getBlackDuckProjectsActionResponse(blackDuckConfigId, pageNumber, pageSize, searchTerm);
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        Optional<String> providerName = fieldModel.getFieldValue(ChannelDescriptor.KEY_PROVIDER_TYPE);
        Optional<String> providerConfigId = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);

        Set<AlertFieldStatus> errors = new HashSet<>();
        if (providerName.isEmpty()) {
            AlertFieldStatus missingProviderName = AlertFieldStatus.error(ProviderDescriptor.KEY_CONFIGURED_PROJECT, String.format("Missing %s", ChannelDescriptor.KEY_PROVIDER_TYPE));
            errors.add(missingProviderName);
        }

        if (providerConfigId.isEmpty()) {
            AlertFieldStatus missingProviderConfigId = AlertFieldStatus.error(ProviderDescriptor.KEY_CONFIGURED_PROJECT, MISSING_PROVIDER_ERROR);
            errors.add(missingProviderConfigId);
        }

        return errors;
    }

    private void validateBlackDuckConfiguration(Long blackDuckConfigId) {
        BlackDuckProperties blackDuckProperties = blackDuckPropertiesFactory.createProperties(blackDuckConfigId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The BlackDuck configuration used in this Job does not exist"));

        BlackDuckApiTokenValidator blackDuckAPITokenValidator = new BlackDuckApiTokenValidator(blackDuckProperties);
        if (!blackDuckAPITokenValidator.isApiTokenValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid permissions. The BlackDuck user configured would not have proper access to notifications for these projects.");
        }
    }

    private ActionResponse<ProviderProjectOptions> getBlackDuckProjectsActionResponse(Long blackDuckGlobalConfigId, int pageNumber, int pageSize, String searchTerm) {
        AlertPagedModel<ProviderProject> providerProjectsPage = providerDataAccessor.getProjectsByProviderConfigId(blackDuckGlobalConfigId, pageNumber, pageSize, searchTerm);
        List<ProviderProjectSelectOption> options = providerProjectsPage.getModels()
            .stream()
            .map(project -> new ProviderProjectSelectOption(project.getName(), project.getHref(), project.getDescription()))
            .collect(Collectors.toList());
        return new ActionResponse<>(HttpStatus.OK, new ProviderProjectOptions(providerProjectsPage.getTotalPages(), providerProjectsPage.getCurrentPage(), providerProjectsPage.getPageSize(), options));
    }

}
