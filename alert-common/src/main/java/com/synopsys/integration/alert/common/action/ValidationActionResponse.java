/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class ValidationActionResponse extends ActionResponse<ValidationResponseModel> {

    public static ValidationActionResponse createResponseFromIntegrationRestException(IntegrationRestException integrationRestException) {
        String exceptionMessage = integrationRestException.getMessage();
        String message = exceptionMessage;
        if (StringUtils.isNotBlank(integrationRestException.getHttpStatusMessage())) {
            message += ": " + integrationRestException.getHttpStatusMessage();
        }
        return new ValidationActionResponse(HttpStatus.valueOf(integrationRestException.getHttpStatusCode()), ValidationResponseModel.generalError(message));
    }

    public static ValidationActionResponse createOKResponseWithContent(ValidationActionResponse response) {
        // TODO A better API for validation. Validation response should not be null.
        return new ValidationActionResponse(HttpStatus.OK, response.getContent().orElse(null));
    }

    public ValidationActionResponse(HttpStatus httpStatus, ValidationResponseModel content) {
        super(httpStatus, null, content);
    }

    @Override
    public Optional<String> getMessage() {
        return getContent()
                   .map(ValidationResponseModel::getMessage);
    }
}
