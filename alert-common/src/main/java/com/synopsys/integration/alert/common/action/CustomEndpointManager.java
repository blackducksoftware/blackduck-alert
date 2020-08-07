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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class CustomEndpointManager {
    public static final String CUSTOM_ENDPOINT_URL = "/api/function";
    private final Map<String, BiFunction<FieldModel, HttpServletContentWrapper, ResponseEntity<String>>> endpointFunctions = new HashMap<>();

    public boolean containsFunction(String functionKey) {
        return endpointFunctions.containsKey(functionKey);
    }

    public void registerFunction(String functionKey, Function<FieldModel, ResponseEntity<String>> endpointFunction) throws AlertException {
        registerFunction(functionKey, (fieldModel, ignoredServletContent) -> endpointFunction.apply(fieldModel));
    }

    public void registerFunction(String functionKey, BiFunction<FieldModel, HttpServletContentWrapper, ResponseEntity<String>> endpointFunction) throws AlertException {
        if (containsFunction(functionKey)) {
            throw new AlertException("A custom endpoint is already registered for " + functionKey);
        }
        endpointFunctions.put(functionKey, endpointFunction);
    }

    public ResponseEntity<String> performFunction(String endpointKey, FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        if (!containsFunction(endpointKey)) {
            return new ResponseEntity<>("No functionality has been created for this endpoint.", HttpStatus.NOT_IMPLEMENTED);
        }
        return endpointFunctions.get(endpointKey).apply(fieldModel, servletContentWrapper);
    }

}
