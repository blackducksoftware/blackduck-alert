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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.action.ActionResult;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public abstract class SimpleCustomEndpoint<R> extends CustomEndpoint<R> {
    public SimpleCustomEndpoint(String fieldKey, CustomEndpointManager customEndpointManager) throws AlertException {
        super(fieldKey, customEndpointManager);
    }

    protected abstract R createData(FieldModel fieldModel) throws AlertException, ResponseStatusException;

    protected abstract ActionResult<R> createErrorResponse(Exception e);

    protected abstract ActionResult<R> createSuccessResponse(R response);

    @Override
    public final ActionResult<R> createResponse(FieldModel fieldModel, HttpServletContentWrapper ignoredServletContent) {
        try {
            R response = createData(fieldModel);
            return createSuccessResponse(response);
        } catch (Exception e) {
            // TODO we won't need this going forward.
            if (e instanceof ResponseStatusException) {
                ResponseStatusException responseStatusException = (ResponseStatusException) e;
                return new ActionResult<>(responseStatusException.getStatus(), responseStatusException.getReason());
            }
            return createErrorResponse(e);
        }
    }

}
