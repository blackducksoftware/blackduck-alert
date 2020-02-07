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
package com.synopsys.integration.alert.workflow.filter;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.builder.JsonFilterBuilder;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;

public class JsonFieldFilterBuilder implements JsonFilterBuilder {
    private final Logger logger = LoggerFactory.getLogger(JsonFieldFilterBuilder.class);

    private final JsonExtractor jsonExtractor;
    private final JsonField jsonField;
    private final String value;

    public JsonFieldFilterBuilder(final JsonExtractor jsonExtractor, final JsonField jsonField, final String value) {
        this.jsonExtractor = jsonExtractor;
        this.jsonField = jsonField;
        this.value = value;
    }

    @Override
    public Predicate<AlertNotificationWrapper> buildPredicate() {
        return notification -> {
            final List<String> contentValues = jsonExtractor.getValuesFromJson(jsonField, notification.getContent());
            logger.debug("Comparing values {} to the configured matcher: {}", contentValues, value);
            for (final String contentValue : contentValues) {
                if (StringUtils.isNotBlank(contentValue) && (contentValue.equals(value.trim()) || contentValue.matches(value))) {
                    logger.debug("Match: {}", contentValue);
                    return true;
                }
            }
            logger.debug("No value matched");
            return false;
        };
    }
}
