/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.action;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.action.FieldModelTestAction;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.exception.IntegrationException;

@Deprecated(forRemoval = true)
public abstract class JiraGlobalFieldModelTestAction extends FieldModelTestAction {
    public static final String JIRA_ADMIN_PERMISSION_NAME = "ADMINISTER";

    protected abstract boolean isAppCheckEnabled(FieldUtility fieldUtility);

    protected abstract boolean isAppMissing(FieldUtility fieldUtility) throws IntegrationException;

    protected abstract boolean canUserGetIssues(FieldUtility fieldUtility) throws IntegrationException;

    protected abstract boolean isUserAdmin(FieldUtility fieldUtility) throws IntegrationException;

    protected abstract String getChannelDisplayName();

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        try {
            if (!canUserGetIssues(registeredFieldValues)) {
                throw new AlertException("User does not have access to view any issues in Jira.");
            }

            if (isAppCheckEnabled(registeredFieldValues)) {
                if (!isUserAdmin(registeredFieldValues)) {
                    throw new AlertException("The configured user must be an admin if 'Plugin Check' is enabled");
                }

                if (isAppMissing(registeredFieldValues)) {
                    throw new AlertException(String.format("Please configure the '%s' plugin for your server.", JiraConstants.JIRA_ALERT_APP_NAME));
                }
            }
        } catch (IntegrationException e) {
            throw new AlertException("An error occurred during testing: " + e.getMessage());
        }
        return new MessageResult(String.format("Successfully connected to %s instance.", getChannelDisplayName()));
    }

}
