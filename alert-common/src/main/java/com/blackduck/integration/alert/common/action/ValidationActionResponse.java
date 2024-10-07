/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.action;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.rest.exception.IntegrationRestException;

public class ValidationActionResponse extends ActionResponse<ValidationResponseModel> {
    public static ValidationActionResponse createResponseFromIntegrationRestException(IntegrationRestException integrationRestException) {
        String exceptionMessage = integrationRestException.getMessage();
        String message = (StringUtils.isNotBlank(exceptionMessage)) ? exceptionMessage : "";
        if (StringUtils.isNotBlank(integrationRestException.getHttpStatusMessage())) {
            if (StringUtils.isNotBlank(message)) {
                message += ": ";
            }
            message += integrationRestException.getHttpStatusMessage();
        }
        return new ValidationActionResponse(HttpStatus.valueOf(integrationRestException.getHttpStatusCode()), ValidationResponseModel.generalError(message));
    }

    public static ValidationActionResponse createOKResponseWithContent(ValidationActionResponse response) {
        // TODO A better API for validation. Validation response should not be null.
        return new ValidationActionResponse(HttpStatus.OK, response.getContent().orElse(null));
    }

    public ValidationActionResponse(ValidationResponseModel content) {
        this(HttpStatus.OK, content);
    }

    // TODO there's no need for a status code here; if there isn't an unexpected exception, this will always be HttpStatus.OK
    public ValidationActionResponse(HttpStatus httpStatus, ValidationResponseModel content) {
        super(httpStatus, null, content);
    }

    @Override
    public Optional<String> getMessage() {
        return getContent()
                   .map(ValidationResponseModel::getMessage);
    }

    public boolean hasValidationErrors() {
        return getContent().stream()
                   .anyMatch(ValidationResponseModel::hasErrors);
    }
}
