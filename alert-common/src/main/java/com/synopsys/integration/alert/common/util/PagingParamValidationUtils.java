/*
 * alert-common
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
package com.synopsys.integration.alert.common.util;

import java.util.Optional;

import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;

public final class PagingParamValidationUtils {
    // TODO find a good home for this
    public static <T> Optional<ActionResponse<T>> createErrorActionResponseIfInvalid(Integer pageNumber, Integer pageSize) {
        StringBuilder messageBuilder = new StringBuilder();
        if (pageNumber < 0) {
            messageBuilder.append("The parameter 'pageNumber' cannot be negative. ");
        }

        if (pageSize < 1) {
            messageBuilder.append("The parameter 'pageSize' must be greater than 0.");
        }

        String errorMessage = messageBuilder.toString();
        if (errorMessage.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ActionResponse<>(HttpStatus.BAD_REQUEST, errorMessage));
    }

    private PagingParamValidationUtils() {
        // This class should not be instantiated
    }

}
