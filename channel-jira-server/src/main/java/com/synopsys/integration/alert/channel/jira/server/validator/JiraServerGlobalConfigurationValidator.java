/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@Component
public class JiraServerGlobalConfigurationValidator {

    public ValidationResponseModel validate(JiraServerGlobalConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(model.getUrl())) {
            statuses.add(AlertFieldStatus.error("url", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isNotBlank(model.getUrl())) {
            try {
                new URL(model.getUrl());
            } catch (MalformedURLException e) {
                statuses.add(AlertFieldStatus.error("url", e.getMessage()));
            }
        }
        if (StringUtils.isBlank(model.getUserName())) {
            statuses.add(AlertFieldStatus.error("username", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getPassword().isEmpty() && !model.getIsPasswordSet().orElse(Boolean.FALSE)) {
            statuses.add(AlertFieldStatus.error("password", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
