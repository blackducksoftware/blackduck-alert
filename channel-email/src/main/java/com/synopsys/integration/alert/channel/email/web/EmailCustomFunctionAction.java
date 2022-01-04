/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.PagedCustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class EmailCustomFunctionAction extends PagedCustomFunctionAction<EmailAddressOptions> {
    private final Logger logger = LoggerFactory.getLogger(EmailCustomFunctionAction.class);
    private final ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailCustomFunctionAction(AuthorizationManager authorizationManager, ProviderDataAccessor providerDataAccessor) {
        super(authorizationManager);
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public ActionResponse<EmailAddressOptions> createPagedActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper, int pageNumber, int pageSize, String searchTerm) {
        Long providerConfigId = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
            .map(Long::parseLong)
            .orElse(null);

        if (null == providerConfigId) {
            logger.debug("Received provider user email data request with a blank provider config name");
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, "You must select a provider config to populate data.");
        }

        try {
            AlertPagedModel<ProviderUserModel> pageOfUsers = providerDataAccessor.getUsersByProviderConfigId(providerConfigId, pageNumber, pageSize, searchTerm);
            if (pageOfUsers.getModels().isEmpty()) {
                logger.debug("No user emails found in the database for the provider with id: {}", providerConfigId);
            }
            List<EmailAddressSelectOption> options = pageOfUsers.getModels()
                .stream()
                .map(providerUser -> new EmailAddressSelectOption(providerUser.getEmailAddress(), providerUser.getOptOut()))
                .collect(Collectors.toList());
            EmailAddressOptions optionList = new EmailAddressOptions(pageOfUsers.getTotalPages(), pageNumber, pageSize, options);
            return new ActionResponse<>(HttpStatus.OK, optionList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        Optional<String> fieldValue = fieldModel.getFieldValue(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        if (fieldValue.isEmpty()) {
            AlertFieldStatus error = AlertFieldStatus.error(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, String.format("Missing %s", ProviderDescriptor.LABEL_PROVIDER_CONFIG_NAME));
            return Set.of(error);
        }
        return Set.of();
    }

}
