/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.provider;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Deprecated
@Component
public class ProviderNameSelectCustomFunctionAction extends CustomFunctionAction<LabelValueSelectOptions> {

    @Autowired
    public ProviderNameSelectCustomFunctionAction(AuthorizationManager authorizationManager, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility) {
        super(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, authorizationManager, descriptorMap, fieldValidationUtility);
    }

    @Override
    public ActionResponse<LabelValueSelectOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        // FIXME removed this as it's no longer being used. Should be deleted before release
        return new ActionResponse(HttpStatus.METHOD_NOT_ALLOWED, List.of());
    }

}
