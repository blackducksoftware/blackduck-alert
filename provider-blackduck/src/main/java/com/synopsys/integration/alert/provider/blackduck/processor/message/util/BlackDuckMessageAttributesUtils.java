/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import java.util.Objects;
import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.rest.HttpUrl;

public final class BlackDuckMessageAttributesUtils {
    public static LinkableItem extractLicense(ProjectVersionComponentVersionView projectVersionComponentVersionView) {
        return projectVersionComponentVersionView.getLicenses()
            .stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(license -> new LinkableItem(BlackDuckMessageLabels.LABEL_LICENSE, license.getLicenseDisplay()))
            .orElse(new LinkableItem(BlackDuckMessageLabels.LABEL_LICENSE, BlackDuckMessageLabels.VALUE_UNKNOWN_LICENSE));
    }

    public static String extractUsage(ProjectVersionComponentVersionView projectVersionComponentVersionView) {
        return projectVersionComponentVersionView.getUsages()
            .stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(UsageType::prettyPrint)
            .orElse(BlackDuckMessageLabels.VALUE_UNKNOWN_USAGE);
    }

    public static Optional<String> extractIssuesUrl(ProjectVersionComponentVersionView bomComponent) {
        return bomComponent.getFirstLinkSafely(ProjectVersionComponentVersionView.COMPONENT_ISSUES_LINK).map(HttpUrl::toString);
    }

    private BlackDuckMessageAttributesUtils() {
    }

}
