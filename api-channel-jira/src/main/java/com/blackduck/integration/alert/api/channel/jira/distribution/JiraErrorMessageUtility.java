/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.exception.AlertFieldException;
import com.blackduck.integration.rest.exception.IntegrationRestException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JiraErrorMessageUtility {
    private final Gson gson;
    private final JiraCustomFieldResolver customFieldResolver;

    public JiraErrorMessageUtility(Gson gson, JiraCustomFieldResolver customFieldResolver) {
        this.gson = gson;
        this.customFieldResolver = customFieldResolver;
    }

    public AlertException improveRestException(IntegrationRestException restException, String issueCreatorFieldKey, String issueCreatorEmail) {
        String message = restException.getMessage();
        try {
            List<String> responseErrors = extractErrorsFromResponseContent(restException.getHttpResponseContent(), issueCreatorFieldKey, issueCreatorEmail);
            if (!responseErrors.isEmpty()) {
                String responseErrorString = StringUtils.join(responseErrors, ", ");
                if (responseErrorString.contains("customfield_")) {
                    for (String customFieldId : customFieldResolver.getCustomFieldIds()) {
                        Optional<String> resolvedCustomFieldIdToName = customFieldResolver.resolveCustomFieldIdToName(customFieldId);
                        if (resolvedCustomFieldIdToName.isPresent()) {
                            String name = resolvedCustomFieldIdToName.get();
                            responseErrorString = responseErrorString.replace(String.format("'%s'", customFieldId), String.format("'%s' ('%s')", customFieldId, name));
                        }
                    }
                }
                message += " | Details: " + responseErrorString;
            }
        } catch (AlertFieldException reporterException) {
            return reporterException;
        }
        return new AlertException(message, restException);
    }

    private List<String> extractErrorsFromResponseContent(String httpResponseContent, String issueCreatorFieldKey, String issueCreatorEmail) throws AlertFieldException {
        JsonObject responseContentObject = gson.fromJson(httpResponseContent, JsonObject.class);
        if (null != responseContentObject && responseContentObject.has("errors")) {
            return extractSpecificErrorsFromErrorsObject(responseContentObject.getAsJsonObject("errors"), issueCreatorFieldKey, issueCreatorEmail);
        }
        return List.of();
    }

    private List<String> extractSpecificErrorsFromErrorsObject(JsonObject errors, String issueCreatorFieldKey, String issueCreatorEmail) throws AlertFieldException {
        List<String> responseErrors = new ArrayList<>();
        if (errors.has("reporter")) {
            throw new AlertFieldException(List.of(
                AlertFieldStatus.error(issueCreatorFieldKey,
                    String.format("There was a problem assigning '%s' to the issue. Please ensure that the user is assigned to the project and has permission to transition issues. Error: %s", issueCreatorEmail, errors.get("reporter")))
            ));
        } else {
            List<String> fieldErrors = errors.entrySet()
                .stream()
                .map(entry -> String.format("Field '%s' has error %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            responseErrors.addAll(fieldErrors);
        }

        if (errors.has("errorMessages")) {
            JsonArray errorMessages = errors.getAsJsonArray("errorMessages");
            for (JsonElement errorMessage : errorMessages) {
                responseErrors.add(errorMessage.getAsString());
            }
        }
        return responseErrors;
    }

}
