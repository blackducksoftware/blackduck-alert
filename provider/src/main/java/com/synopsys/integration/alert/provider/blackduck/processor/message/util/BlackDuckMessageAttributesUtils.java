/*
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.rest.HttpUrl;

public final class BlackDuckMessageAttributesUtils {
    public static LinkableItem extractLicense(ProjectVersionComponentView bomComponent) {
        return bomComponent.getLicenses()
                   .stream()
                   .findFirst()
                   .map(license -> new LinkableItem(BlackDuckMessageLabels.LABEL_LICENSE, license.getLicenseDisplay(), license.getLicense()))
                   .orElse(new LinkableItem(BlackDuckMessageLabels.LABEL_LICENSE, BlackDuckMessageLabels.VALUE_UNKNOWN_LICENSE));
    }

    public static String extractUsage(ProjectVersionComponentView bomComponent) {
        return bomComponent.getUsages()
                   .stream()
                   .findFirst()
                   .map(UsageType::prettyPrint)
                   .orElse(BlackDuckMessageLabels.VALUE_UNKNOWN_USAGE);
    }

    public static Optional<String> extractIssuesUrl(ProjectVersionComponentView bomComponent) {
        return bomComponent.getFirstLinkSafely(ProjectVersionComponentView.COMPONENT_ISSUES_LINK).map(HttpUrl::toString);
    }

    private BlackDuckMessageAttributesUtils() {
    }

}
