/*
 * channel-jira-server
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraServerGlobalConfigurationValidator implements GlobalConfigurationValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        ConfigurationFieldValidator.validateIsARequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_URL).ifPresent(statuses::add);
        ConfigurationFieldValidator.validateIsARequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_USERNAME).ifPresent(statuses::add);
        ConfigurationFieldValidator.validateIsARequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_PASSWORD).ifPresent(statuses::add);

        return statuses;
    }
}
