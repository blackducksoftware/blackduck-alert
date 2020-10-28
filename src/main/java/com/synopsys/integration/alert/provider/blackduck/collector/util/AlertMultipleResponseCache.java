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

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.LinkMultipleResponses;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.exception.IntegrationException;

public class AlertMultipleResponseCache {
    private final BlackDuckService blackDuckService;
    private final AlertMultipleBucket alertMultipleBucket;

    public AlertMultipleResponseCache(BlackDuckService blackDuckService, AlertMultipleBucket alertMultipleBucket) {
        this.blackDuckService = blackDuckService;
        this.alertMultipleBucket = alertMultipleBucket;
    }

    public <T extends BlackDuckResponse> AlertMultipleBucketItem<T> get(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        Optional<String> linkOptional = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        if (!linkOptional.isPresent()) {
            return new AlertMultipleBucketItem(linkMultipleResponses.getLink());
        }
        String uri = linkOptional.get();

        return get(uri, linkMultipleResponses.getResponseClass());
    }

    public <T extends BlackDuckResponse> AlertMultipleBucketItem<T> get(String uri, Class<T> responseClass) throws IntegrationException {
        if (alertMultipleBucket.contains(uri)) {
            AlertMultipleBucketItem responses = alertMultipleBucket.get(uri);
            if (null != responses) {
                return responses;
            }
        }
        List<T> allResponses = blackDuckService.getAllResponses(uri, responseClass);
        AlertMultipleBucketItem alertMultipleBucketItem = new AlertMultipleBucketItem(uri, allResponses);
        alertMultipleBucket.add(uri, new AlertMultipleBucketItem(uri, allResponses));
        return alertMultipleBucketItem;
    }

}
