package com.synopsys.integration.alert.provider.blackduck.collector.builder.util;

import java.util.Optional;

import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public final class BlackDuckIssueTrackerCallbackUtil {
    public static final String COMPONENT_ISSUES_LINK_NAME = "component-issues";

    private BlackDuckIssueTrackerCallbackUtil() {
    }

    public static Optional<ComponentItemCallbackInfo> createCallbackInfo(NotificationType notificationType, ProjectVersionComponentView projectVersionComponentView) {
        return projectVersionComponentView.getFirstLink(COMPONENT_ISSUES_LINK_NAME)
                   .map(componentIssuesLink -> new ComponentItemCallbackInfo(componentIssuesLink, new BlackDuckProviderKey(), notificationType.name()));
    }

}
