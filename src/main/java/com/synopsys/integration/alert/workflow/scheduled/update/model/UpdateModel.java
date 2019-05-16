/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.workflow.scheduled.update.model;

import org.apache.commons.lang3.StringUtils;

public class UpdateModel {
    private final String currentVersion;
    private final String latestAvailableVersion;
    private final String repositoryUrl;

    public UpdateModel(final String currentVersion, final String latestAvailableVersion, final String repositoryUrl) {
        this.currentVersion = currentVersion;
        this.latestAvailableVersion = latestAvailableVersion;
        this.repositoryUrl = repositoryUrl;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestAvailableVersion() {
        return latestAvailableVersion;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public boolean isUpdatable() {
        if (StringUtils.isAnyBlank(currentVersion, latestAvailableVersion)) {
            return false;
        }
        return !currentVersion.equals(latestAvailableVersion);
    }

}
