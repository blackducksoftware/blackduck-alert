/**
 * web
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
package com.synopsys.integration.alert.web.api.about;

import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class AboutModel extends AlertSerializableModel {
    private String version;
    private String created;
    private String description;
    private String projectUrl;
    private String documentationUrl;
    private boolean initialized;
    private String startupTime;
    private Set<DescriptorMetadata> providers;
    private Set<DescriptorMetadata> channels;

    protected AboutModel() {

    }

    public AboutModel(
        String version,
        String created,
        String description,
        String projectUrl,
        String documentationUrl,
        boolean initialized,
        String startupTime,
        Set<DescriptorMetadata> providers,
        Set<DescriptorMetadata> channels
    ) {
        this.version = version;
        this.created = created;
        this.description = description;
        this.projectUrl = projectUrl;
        this.documentationUrl = documentationUrl;
        this.initialized = initialized;
        this.startupTime = startupTime;
        this.providers = providers;
        this.channels = channels;
    }

    public String getVersion() {
        return version;
    }

    public String getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getStartupTime() {
        return startupTime;
    }

    public Set<DescriptorMetadata> getProviders() {
        return providers;
    }

    public Set<DescriptorMetadata> getChannels() {
        return channels;
    }

}
