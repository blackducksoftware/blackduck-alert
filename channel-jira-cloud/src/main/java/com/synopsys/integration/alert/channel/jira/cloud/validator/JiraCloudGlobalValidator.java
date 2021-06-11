/*
 * channel-jira-cloud
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.FieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraCloudGlobalValidator extends GlobalValidator {

    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        FieldValidator.validateIsARequiredField(fieldModel, JiraCloudDescriptor.KEY_JIRA_URL).ifPresent(statuses::add);
        FieldValidator.validateIsARequiredField(fieldModel, JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS).ifPresent(statuses::add);
        FieldValidator.validateIsARequiredField(fieldModel, JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN).ifPresent(statuses::add);

        return statuses;
    }

}
