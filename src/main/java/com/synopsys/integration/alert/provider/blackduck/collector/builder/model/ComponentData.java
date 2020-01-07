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
package com.synopsys.integration.alert.provider.blackduck.collector.builder.model;

import java.util.Optional;

public class ComponentData {
    private String componentName;
    private String componentVersionName;
    private String projectVersionUrl;
    private String projectComponentLink;

    public ComponentData(String componentName, String componentVersionName, String projectVersionUrl, String projectComponentLink) {
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
        this.projectVersionUrl = projectVersionUrl;
        this.projectComponentLink = projectComponentLink;
    }

    public String getComponentName() {
        return componentName;
    }

    public Optional<String> getComponentVersionName() {
        return Optional.ofNullable(componentVersionName);
    }

    public String getProjectVersionUrl() {
        return projectVersionUrl;
    }

    public String getProjectComponentLink() {
        return projectComponentLink;
    }
}
