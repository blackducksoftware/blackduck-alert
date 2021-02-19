/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopys.integration.alert.channel.api.issue.model;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueCreationModel extends AlertSerializableModel {
    private final String title;
    private final String description;
    private final List<String> postCreateComments;

    private final LinkableItem provider;
    private final ProjectIssueModel source;

    public static IssueCreationModel simple(String title, String description, List<String> postCreateComments, LinkableItem provider) {
        return new IssueCreationModel(title, description, postCreateComments, provider, null);
    }

    public static IssueCreationModel project(String title, String description, List<String> postCreateComments, ProjectIssueModel source) {
        return new IssueCreationModel(title, description, postCreateComments, source.getProvider(), source);
    }

    private IssueCreationModel(String title, String description, List<String> postCreateComments, LinkableItem provider, @Nullable ProjectIssueModel source) {
        this.title = title;
        this.description = description;
        this.postCreateComments = postCreateComments;
        this.provider = provider;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPostCreateComments() {
        return postCreateComments;
    }

    public LinkableItem getProvider() {
        return provider;
    }

    public Optional<ProjectIssueModel> getSource() {
        return Optional.ofNullable(source);
    }

}
