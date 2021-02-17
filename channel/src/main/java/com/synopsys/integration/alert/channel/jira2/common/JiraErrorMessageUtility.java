/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.jira2.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class JiraErrorMessageUtility {
    private final Gson gson;

    @Autowired
    public JiraErrorMessageUtility(Gson gson) {
        this.gson = gson;
    }

    public AlertException improveRestException(IntegrationRestException restException, String issueCreatorFieldKey, String issueCreatorEmail) {
        String message = restException.getMessage();
        try {
            List<String> responseErrors = extractErrorsFromResponseContent(restException.getHttpResponseContent(), issueCreatorFieldKey, issueCreatorEmail);
            if (!responseErrors.isEmpty()) {
                message += " | Details: " + StringUtils.join(responseErrors, ", ");
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
