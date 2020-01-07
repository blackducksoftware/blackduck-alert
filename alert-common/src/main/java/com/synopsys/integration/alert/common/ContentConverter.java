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
package com.synopsys.integration.alert.common;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class ContentConverter {
    private final ConversionService conversionService;
    private final Gson gson;

    @Autowired
    public ContentConverter(final Gson gson, final ConversionService conversionService) {
        this.conversionService = conversionService;
        this.gson = gson;
    }

    public <C> C getJsonContent(final String content, final Class<C> contentClass) {
        if (contentClass != null && content != null) {
            return gson.fromJson(content, contentClass);
        }
        return null;
    }

    public String getJsonString(final Object content) {
        if (content != null) {
            return gson.toJson(content);
        }
        return null;
    }

    public <C> Optional<C> getContent(final Object content, final Class<C> contentClass) {
        if (contentClass != null && content != null && conversionService.canConvert(content.getClass(), contentClass)) {
            return Optional.ofNullable(conversionService.convert(content, contentClass));
        }
        return Optional.empty();
    }

    public <C> C getValue(final Object value, final Class<C> clazz) {
        final Optional<C> content = getContent(value, clazz);
        if (content.isPresent()) {
            return content.get();
        }
        return null;
    }

    public String getStringValue(final Object content) {
        return getValue(content, String.class);
    }

    public <C> C getValueOfString(final String value, final Class<C> clazz) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return getValue(value, clazz);
    }

    public Integer getIntegerValue(final String value) {
        return getValueOfString(value, Integer.class);
    }

    public Long getLongValue(final String value) {
        return getValueOfString(value, Long.class);
    }

    public Boolean getBooleanValue(final String value) {
        return getValueOfString(value, Boolean.class);
    }

    public Boolean isBoolean(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        final String trimmedValue = value.trim();
        return trimmedValue.equalsIgnoreCase("false") || trimmedValue.equalsIgnoreCase("true");
    }
}
