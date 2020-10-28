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
package com.synopsys.integration.alert.provider.blackduck.collector.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.exception.IntegrationException;

public class AlertMultipleBucket {
    private final Map<String, AlertMultipleBucketItem<BlackDuckResponse>> cache = new ConcurrentHashMap<>();

    public boolean contains(String uri) {
        return cache.containsKey(uri);
    }

    public void add(String uri, AlertMultipleBucketItem responses) {
        cache.put(uri, responses);
    }

    public <T extends BlackDuckResponse> AlertMultipleBucketItem get(String uri) {
        return cache.get(uri);
    }

    public <T extends BlackDuckResponse> AlertMultipleBucketItem<T> get(String uri, Class<T> responseClass) throws IntegrationException {
        if (contains(uri)) {
            AlertMultipleBucketItem responses = get(uri);
            if (null != responses) {
                return responses;
            }
        }
        return new AlertMultipleBucketItem<>(uri);
    }

}
