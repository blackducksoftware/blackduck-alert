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
package com.blackducksoftware.integration.alert.web.model;

import java.util.Collections;
import java.util.List;

import com.blackducksoftware.integration.util.Stringable;

public class AboutModel extends Stringable {
    private String version;
    private String description;
    private String projectUrl;
    private List<String> channelList;
    private List<String> providerList;

    protected AboutModel() {

    }

    public AboutModel(final String version, final String description, final String projectUrl) {
        this(version, description, projectUrl, Collections.emptyList(), Collections.emptyList());
    }

    public AboutModel(final String version, final String description, final String projectUrl, final List<String> providerList, final List<String> channelList) {
        this.version = version;
        this.description = description;
        this.projectUrl = projectUrl;
        this.channelList = channelList;
        this.providerList = providerList;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public List<String> getChannelList() {
        return channelList;
    }

    public List<String> getProviderList() {
        return providerList;
    }
}
