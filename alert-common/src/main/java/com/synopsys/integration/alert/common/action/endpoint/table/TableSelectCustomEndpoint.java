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
package com.synopsys.integration.alert.common.action.endpoint.table;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResult;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.action.endpoint.SimpleCustomEndpoint;
import com.synopsys.integration.alert.common.exception.AlertException;

public abstract class TableSelectCustomEndpoint extends SimpleCustomEndpoint<List<?>> {

    protected TableSelectCustomEndpoint(String fieldKey, CustomEndpointManager customEndpointManager) throws AlertException {
        super(fieldKey, customEndpointManager);
    }

    @Override
    protected ActionResult<List<?>> createErrorResponse(Exception e) {
        return new ActionResult<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("An internal issue occurred while trying to retrieve your select data: %s", e.getMessage()));
    }

    @Override
    protected ActionResult<List<?>> createSuccessResponse(List<?> response) {
        return new ActionResult<>(HttpStatus.OK, response);
    }

}
