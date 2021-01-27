/**
 * processor-api
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
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class SimpleMessage extends ProviderMessage<SimpleMessage> {
    private final String summary;
    private final String description;
    private final List<LinkableItem> details;

    private final ProjectMessage source;

    public static SimpleMessage original(LinkableItem provider, String summary, String description, List<LinkableItem> details) {
        return new SimpleMessage(provider, summary, description, details, null);
    }

    public static SimpleMessage derived(String summary, String description, List<LinkableItem> details, ProjectMessage source) {
        return new SimpleMessage(source.getProvider(), summary, description, details, source);
    }

    private SimpleMessage(LinkableItem provider, String summary, String description, List<LinkableItem> details, @Nullable ProjectMessage source) {
        super(provider);
        this.summary = summary;
        this.description = description;
        this.details = details;
        this.source = source;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public List<LinkableItem> getDetails() {
        return details;
    }

    public Optional<ProjectMessage> getSource() {
        return Optional.ofNullable(source);
    }

    @Override
    public List<SimpleMessage> combine(SimpleMessage otherMessage) {
        return List.of(this, otherMessage);
    }

}
