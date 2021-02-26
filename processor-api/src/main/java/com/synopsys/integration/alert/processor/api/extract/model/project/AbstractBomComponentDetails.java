/*
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
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public abstract class AbstractBomComponentDetails extends AlertSerializableModel {
    private final LinkableItem component;
    private final LinkableItem componentVersion;

    private final LinkableItem license;
    private final String usage;
    private final List<LinkableItem> additionalAttributes;

    private final String blackDuckIssuesUrl;

    protected AbstractBomComponentDetails(
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        LinkableItem license,
        String usage,
        List<LinkableItem> additionalAttributes,
        String blackDuckIssuesUrl
    ) {
        this.component = component;
        this.componentVersion = componentVersion;
        this.license = license;
        this.usage = usage;
        this.additionalAttributes = additionalAttributes;
        this.blackDuckIssuesUrl = blackDuckIssuesUrl;
    }

    public LinkableItem getComponent() {
        return component;
    }

    public Optional<LinkableItem> getComponentVersion() {
        return Optional.ofNullable(componentVersion);
    }

    public LinkableItem getLicense() {
        return license;
    }

    public String getUsage() {
        return usage;
    }

    public List<LinkableItem> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public String getBlackDuckIssuesUrl() {
        return blackDuckIssuesUrl;
    }

}
