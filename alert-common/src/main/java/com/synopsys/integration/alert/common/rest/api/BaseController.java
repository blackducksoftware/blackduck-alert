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
package com.synopsys.integration.alert.common.rest.api;

import java.util.function.BiFunction;

import org.springframework.web.bind.annotation.RequestMapping;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@RequestMapping(AlertRestConstants.BASE_PATH)
// TODO this class is an improper use of abstraction, it should be removed
public abstract class BaseController {
    protected static final String LOGGER_PATTERN_BREAKING_EXPRESSION = "[\n|\r|\t]";

    public static String createSaferLoggableString(String taintedString) {
        return taintedString.replaceAll(LOGGER_PATTERN_BREAKING_EXPRESSION, "_");
    }

    public boolean hasGlobalPermission(BiFunction<String, String, Boolean> permissionChecker, DescriptorKey descriptorKey) {
        return permissionChecker.apply(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    }

}
