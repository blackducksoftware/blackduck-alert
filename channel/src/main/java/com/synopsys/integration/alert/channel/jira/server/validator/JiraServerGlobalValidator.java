/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.FieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraServerGlobalValidator extends GlobalValidator {
    @Override
    protected Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        AlertFieldStatus urlStatus = FieldValidator.containsRequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_URL);
        AlertFieldStatus usernameStatus = FieldValidator.containsRequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_USERNAME);
        AlertFieldStatus passwordStatus = FieldValidator.containsRequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_PASSWORD);

        return Set.of(urlStatus, usernameStatus, passwordStatus);
    }
}
