/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public abstract class BinaryOperatorFieldFilterBuilder implements JsonFilterBuilder {
    private final JsonFilterBuilder leftFilterBuilder;
    private final JsonFilterBuilder rightFilterBuilder;
    private final BiFunction<Predicate<NotificationContent>, Predicate<NotificationContent>, Predicate<NotificationContent>> binaryOperator;

    public BinaryOperatorFieldFilterBuilder(final JsonFilterBuilder leftFilterBuilder, final JsonFilterBuilder rightFilterBuilder,
        final BiFunction<Predicate<NotificationContent>, Predicate<NotificationContent>, Predicate<NotificationContent>> binaryOperator) {
        this.leftFilterBuilder = leftFilterBuilder;
        this.rightFilterBuilder = rightFilterBuilder;
        this.binaryOperator = binaryOperator;
    }

    @Override
    public final Predicate<NotificationContent> buildPredicate() {
        return binaryOperator.apply(leftFilterBuilder.buildPredicate(), rightFilterBuilder.buildPredicate());
    }
}
