/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpResponseException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;

@Component
public class AzureBoardsHttpExceptionMessageImprover {
    private static final String AZURE_ERROR_MESSAGE_FIELD_NAME = "message";

    private final Gson gson;

    @Autowired
    public AzureBoardsHttpExceptionMessageImprover(Gson gson) {
        this.gson = gson;
    }

    public Optional<String> extractImprovedMessage(HttpServiceException httpServiceException) {
        Throwable cause = httpServiceException.getCause();
        if (cause instanceof HttpResponseException) {
            return extractImprovedMessage((HttpResponseException) cause);
        }
        return Optional.empty();
    }

    public Optional<String> extractImprovedMessage(HttpResponseException httpResponseException) {
        String responseContent = httpResponseException.getContent();
        if (StringUtils.isNotBlank(responseContent)) {
            return extractResponseContentMessage(responseContent);
        }
        return Optional.empty();
    }

    private Optional<String> extractResponseContentMessage(String responseContent) {
        try {
            JsonObject responseContentObject = gson.fromJson(responseContent, JsonObject.class);
            JsonElement messageElement = responseContentObject.get(AZURE_ERROR_MESSAGE_FIELD_NAME);
            return Optional.ofNullable(messageElement)
                       .filter(JsonElement::isJsonPrimitive)
                       .map(JsonElement::getAsJsonPrimitive)
                       .filter(JsonPrimitive::isString)
                       .map(JsonPrimitive::getAsString);
        } catch (JsonParseException e) {
            return Optional.empty();
        }
    }

}
