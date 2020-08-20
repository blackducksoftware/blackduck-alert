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
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OAuthRequestValidator {
    private final Logger logger = LoggerFactory.getLogger(OAuthRequestValidator.class);
    private final Map<String, Instant> requestMap = new ConcurrentHashMap<>();

    public void addAuthorizationRequest(String requestKey) {
        logger.debug("Adding OAuth authorization key {}", requestKey);
        requestMap.put(requestKey, Instant.now());
    }

    public void removeAuthorizationRequest(String requestKey) {
        requestMap.remove(requestKey);
        logger.debug("Removed OAuth authorization key {}", requestKey);
    }

    public boolean hasRequestKey(String requestKey) {
        return requestMap.containsKey(requestKey);
    }

    public void removeRequestsOlderThanInstant(Instant instant) {
        requestMap.entrySet().stream()
            .filter(entry -> entry.getValue().isBefore(instant))
            .map(Map.Entry::getKey)
            .forEach(this::removeAuthorizationRequest);

    }
}
