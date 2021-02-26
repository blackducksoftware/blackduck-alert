/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.provider.project;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.PagedCustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.model.ProviderProjectSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class ProviderProjectCustomFunctionAction extends PagedCustomFunctionAction<ProviderProjectOptions> {
    private static final String MISSING_PROVIDER_ERROR = "Provider name is required to retrieve projects.";

    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public ProviderProjectCustomFunctionAction(AuthorizationManager authorizationManager, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility, ProviderDataAccessor providerDataAccessor) {
        super(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, authorizationManager, descriptorMap, fieldValidationUtility);
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public ActionResponse<ProviderProjectOptions> createPagedActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper, int pageNumber, int pageSize, String searchTerm) {
        String providerName = fieldModel.getFieldValue(ChannelDistributionUIConfig.KEY_PROVIDER_NAME).orElse("");
        if (StringUtils.isBlank(providerName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MISSING_PROVIDER_ERROR);
        }

        return fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
                   .map(Long::parseLong)
                   .map(configId -> getBlackDuckProjectsActionResponse(configId, pageNumber, pageSize, searchTerm))
                   .orElse(new ActionResponse<>(HttpStatus.OK, new ProviderProjectOptions(0, pageNumber, pageSize, List.of())));
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
