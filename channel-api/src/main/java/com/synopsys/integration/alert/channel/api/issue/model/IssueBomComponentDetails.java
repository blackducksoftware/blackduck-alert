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
package com.synopsys.integration.alert.channel.api.issue.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;

public class IssueBomComponentDetails extends AbstractBomComponentDetails {
    private static final String UNKNOWN_USAGE = "Unknown Usage";
    private static final LinkableItem UNKNOWN_LICENSE = new LinkableItem("License", "Unknown License");

    public static IssueBomComponentDetails fromSearchResults(LinkableItem component, @Nullable LinkableItem componentVersion) {
        return new IssueBomComponentDetails(
            component,
            componentVersion,
            UNKNOWN_LICENSE,
            UNKNOWN_USAGE,
            List.of(),
            ""
        );
    }

    public static IssueBomComponentDetails fromBomComponentDetails(AbstractBomComponentDetails bomComponentDetails) {
        return new IssueBomComponentDetails(
            bomComponentDetails.getComponent(),
            bomComponentDetails.getComponentVersion().orElse(null),
            bomComponentDetails.getLicense(),
            bomComponentDetails.getUsage(),
            bomComponentDetails.getAdditionalAttributes(),
            bomComponentDetails.getBlackDuckIssuesUrl()
        );
    }

    public IssueBomComponentDetails(LinkableItem component, @Nullable LinkableItem componentVersion, LinkableItem license, String usage, List<LinkableItem> additionalAttributes, String blackDuckIssuesUrl) {
        super(component, componentVersion, license, usage, additionalAttributes, blackDuckIssuesUrl);
    }

}
