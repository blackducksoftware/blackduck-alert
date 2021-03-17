/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.action;

import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class JiraGlobalTestAction extends TestAction {
    public static final String JIRA_ADMIN_PERMISSION_NAME = "ADMINISTER";

    protected abstract boolean isAppCheckEnabled(FieldUtility fieldUtility);

    protected abstract boolean isAppMissing(FieldUtility fieldUtility) throws IntegrationException;

    protected abstract boolean isUserMissing(FieldUtility fieldUtility) throws IntegrationException;

    protected abstract boolean isUserAdmin(FieldUtility fieldUtility) throws IntegrationException;

    protected abstract String getChannelDisplayName();

    @Override
    public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
        try {
            if (isUserMissing(registeredFieldValues)) {
                throw new AlertException("User did not match any known users.");
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
