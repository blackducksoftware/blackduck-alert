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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;

@Component
public class JiraServerGlobalConfigurationValidator {
    private JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    public JiraServerGlobalConfigurationValidator(JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor) {
        this.jiraServerGlobalConfigAccessor = jiraServerGlobalConfigAccessor;
    }

    public ValidationResponseModel validate(JiraServerGlobalConfigModel model, String id) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        } else if (doesNameExist(model.getName(), id)) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.DUPLICATE_NAME_FOUND));
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
            statuses.add(AlertFieldStatus.error("userName", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getPassword().isEmpty() && !model.getIsPasswordSet().orElse(Boolean.FALSE)) {
            statuses.add(AlertFieldStatus.error("password", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    //Checks if a configuration already exists then checks if we're updating the found configuration
    private boolean doesNameExist(String name, @Nullable String currentConfigId) {
        return jiraServerGlobalConfigAccessor.getConfigurationByName(name)
            .map(JiraServerGlobalConfigModel::getId)
            .filter(id -> (currentConfigId != null) ? !currentConfigId.equals(id) : true)
            .isPresent();
    }
}
