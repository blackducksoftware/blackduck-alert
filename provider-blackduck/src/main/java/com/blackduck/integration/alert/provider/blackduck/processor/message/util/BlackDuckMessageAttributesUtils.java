package com.blackduck.integration.alert.provider.blackduck.processor.message.util;

import java.util.Objects;
import java.util.Optional;

import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.blackduck.integration.blackduck.api.generated.enumeration.UsageType;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.rest.HttpUrl;

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
