/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.controller;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.synopsys.integration.alert.common.SetMap;

@RestController
public class ExposedEndpointsController extends BaseController {
    public final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public ExposedEndpointsController(final RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping
    public Map<String, Set<RequestMethod>> get() {
        SetMap<String, RequestMethod> restMappings = new SetMap(new TreeMap());

        for (final RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            for (final String apiPath : info.getPatternsCondition().getPatterns()) {
                if (apiPath != null) {
                    restMappings.addAll(apiPath, info.getMethodsCondition().getMethods());
                }
            }
        }
        return restMappings.getMap();
    }

}
