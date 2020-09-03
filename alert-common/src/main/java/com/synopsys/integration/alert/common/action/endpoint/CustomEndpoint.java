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
package com.synopsys.integration.alert.common.action.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.action.ActionResult;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.exception.IntegrationException;

public abstract class CustomEndpoint<T> {
    public static final String API_FUNCTION_URL = "/api/function";
    private AuthorizationManager authorizationManager;

    public CustomEndpoint(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public ActionResult<T> createResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            if (!authorizationManager.hasExecutePermission(fieldModel.getContext(), fieldModel.getDescriptorName())) {
                return new ActionResult<>(HttpStatus.FORBIDDEN, ResponseFactory.UNAUTHORIZED_REQUEST_MESSAGE);
            }
            return createActionResponse(fieldModel, servletContentWrapper);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                ResponseStatusException responseStatusException = (ResponseStatusException) e;
                return new ActionResult<>(responseStatusException.getStatus(), responseStatusException.getReason());
            }
            return createErrorResponse(e);
        }
    }

    private ActionResult<T> createErrorResponse(Exception e) {
        return new ActionResult<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("An internal issue occurred while trying to retrieve your data: %s", e.getMessage()));
    }

    public abstract ActionResult<T> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws IntegrationException;

}
