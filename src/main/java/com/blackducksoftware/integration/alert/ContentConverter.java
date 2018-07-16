/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class ContentConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;

    @Autowired
    public ContentConverter(final Gson gson) {
        this.gson = gson;
    }

    public <C> Optional<C> getContent(final String content, final Class<C> contentClass) {
        if (contentClass != null && content != null) {
            return Optional.ofNullable(gson.fromJson(content, contentClass));
        }
        return Optional.empty();
    }

    public <C> String convertToString(final C content) {
        if (content == null) {
            return null;
        }
        return gson.toJson(content);
    }

    public Integer getInteger(final String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                final Integer intValue = Integer.valueOf(value);
                return intValue;
            } catch (final NumberFormatException e) {
                logger.debug("Passed value is not an integer value");
            }
        }
        return null;
    }

    public Long getLong(final String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                final Long longValue = Long.valueOf(value);
                return longValue;
            } catch (final NumberFormatException e) {
                logger.debug("Passed value is not a long value");
            }
        }
        return null;
    }
}
