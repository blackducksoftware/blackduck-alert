/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;

@Component
public class JiraServerGlobalValidationAction {
    private final JiraServerGlobalConfigurationValidator validator;
    private final ConfigurationValidationHelper validationHelper;

    @Autowired
    public JiraServerGlobalValidationAction(JiraServerGlobalConfigurationValidator validator, AuthorizationManager authorizationManager) {
        this.validator = validator;
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.JIRA_SERVER);
    }

    public ActionResponse<ValidationResponseModel> validate(JiraServerGlobalConfigModel requestResource) {
        return validationHelper.validate(() -> validator.validate(requestResource, requestResource.getId()));
    }
}
