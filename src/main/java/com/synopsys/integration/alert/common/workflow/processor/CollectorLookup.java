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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.enumeration.FormatType;

public class CollectorLookup {
    private final Map<FormatType, TopicCollector> formatCollectorMap;

    public CollectorLookup(final List<TopicCollector> topicCollectorList) {
        formatCollectorMap = topicCollectorList.stream().collect(Collectors.toMap(TopicCollector::getFormat, Function.identity()));
    }

    public Optional<TopicCollector> findCollector(final FormatType formatType) {
        if (formatCollectorMap.containsKey(formatType)) {
            return Optional.of(formatCollectorMap.get(formatType));
        } else {
            return Optional.empty();
        }
    }
}
