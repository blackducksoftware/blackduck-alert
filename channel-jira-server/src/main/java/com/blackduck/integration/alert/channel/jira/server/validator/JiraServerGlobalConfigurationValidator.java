/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;

@Component
public class JiraServerGlobalConfigurationValidator {
    private static final String JIRA_TIMEOUT_INVALID_ERROR_MESSAGE = "Jira server timeout must be a positive integer.";

    private final JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor;

    @Autowired
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
        model.getTimeout().ifPresent(timeout -> {
            if (timeout < 1) {
                statuses.add(AlertFieldStatus.error("timeout", JIRA_TIMEOUT_INVALID_ERROR_MESSAGE));
            }
        });
        if (StringUtils.isNotBlank(model.getUrl())) {
            try {
                new URL(model.getUrl());
            } catch (MalformedURLException e) {
                statuses.add(AlertFieldStatus.error("url", e.getMessage()));
            }
        }

        validateAuthenticationFields(model, statuses);

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    private void validateAuthenticationFields(JiraServerGlobalConfigModel model, Set<AlertFieldStatus> statuses) {
        if (model.getAuthorizationMethod() == null) {
            statuses.add(AlertFieldStatus.error("authorizationMethod", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            return;
        }

        if (model.getAuthorizationMethod() == JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN) {
            if (model.getAccessToken().isEmpty() && !model.getIsAccessTokenSet().orElse(Boolean.FALSE)) {
                statuses.add(AlertFieldStatus.error("accessToken", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
            }
            //If access token is used, do not evaluate basic auth credentials
            return;
        }
        if (model.getUserName().filter(Predicate.not(String::isEmpty)).isEmpty()) {
            statuses.add(AlertFieldStatus.error("userName", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getPassword().isEmpty() && !model.getIsPasswordSet().orElse(Boolean.FALSE)) {
            statuses.add(AlertFieldStatus.error("password", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
    }

    //Checks if a configuration already exists then checks if we're updating the found configuration
    private boolean doesNameExist(String name, @Nullable String currentConfigId) {
        return jiraServerGlobalConfigAccessor.getConfigurationByName(name)
            .map(JiraServerGlobalConfigModel::getId)
            .filter(id -> currentConfigId == null || !currentConfigId.equals(id))
            .isPresent();
    }
}
