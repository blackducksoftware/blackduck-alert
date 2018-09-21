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
package com.synopsys.integration.alert.workflow.filter.builder;

import java.util.List;
import java.util.function.Predicate;

import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;

public class JsonFieldFilterBuilder implements JsonFilterBuilder {
    private final JsonExtractor jsonExtractor;
    private final StringHierarchicalField hierarchicalField;
    private final String value;

    public JsonFieldFilterBuilder(final JsonExtractor jsonExtractor, final StringHierarchicalField hierarchicalField, final String value) {
        this.jsonExtractor = jsonExtractor;
        this.hierarchicalField = hierarchicalField;
        this.value = value;
    }

    @Override
    public Predicate<NotificationContent> buildPredicate() {
        final Predicate<NotificationContent> contentPredicate = (notification) -> {
            final List<String> contentValues = jsonExtractor.getValuesFromJson(hierarchicalField, notification.getContent());
            for (final String contentValue : contentValues) {
                if (value.equals(contentValue)) {
                    return true;
                }
            }
            return false;
        };
        return contentPredicate;
    }
}
