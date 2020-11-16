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

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.PagingParamValidationUtils;
import com.synopsys.integration.exception.IntegrationException;

public abstract class PagedCustomFunctionAction<T extends AlertPagedModel<?>> extends CustomFunctionAction<T> {
    public PagedCustomFunctionAction(String fieldKey, AuthorizationManager authorizationManager, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility) {
        super(fieldKey, authorizationManager, descriptorMap, fieldValidationUtility);
    }

    @Override
    public final ActionResponse<T> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws IntegrationException {
        HttpServletRequest httpRequest = servletContentWrapper.getHttpRequest();
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();

        int pageNumber = extractIntParam(parameterMap, "pageNumber", 0);
        int pageSize = extractIntParam(parameterMap, "pageSize", 10);
        Optional<ActionResponse<T>> pageRequestError = PagingParamValidationUtils.createErrorActionResponseIfInvalid(pageNumber, pageSize);
        if (pageRequestError.isPresent()) {
            return pageRequestError.get();
        }

        String searchTerm = extractFirstParam(parameterMap, "searchTerm").orElse("");
        return createPagedActionResponse(fieldModel, servletContentWrapper, pageNumber, pageSize, searchTerm);
    }

    protected abstract ActionResponse<T> createPagedActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper, int pageNumber, int pageSize, String searchTerm) throws IntegrationException;

    protected final Optional<String> extractFirstParam(Map<String, String[]> parameterMap, String paramName) {
        return Optional.ofNullable(parameterMap.get(paramName))
                   .filter(paramValues -> paramValues.length > 0)
                   .map(paramValues -> paramValues[0]);
    }

    protected int extractIntParam(Map<String, String[]> parameterMap, String paramName, int defaultValue) {
        return extractFirstParam(parameterMap, paramName)
                   .filter(NumberUtils::isDigits)
                   .map(NumberUtils::toInt)
                   .orElse(defaultValue);
    }

}
