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

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.util.Stringable;

public class AlertMultipleBucketItem<T extends BlackDuckResponse> extends Stringable {
    private final String uri;
    private final List<T> blackDuckResponses;

    public AlertMultipleBucketItem(String uri, List<T> blackDuckResponses) {
        this.uri = uri;
        this.blackDuckResponses = blackDuckResponses;
    }

    public AlertMultipleBucketItem(String uri) {
        this.uri = uri;
        this.blackDuckResponses = List.of();
    }

    public String getUri() {
        return uri;
    }

    public List<T> getBlackDuckResponses() {
        return blackDuckResponses;
    }

    public boolean isEmpty() {
        return blackDuckResponses.isEmpty();
    }

}
