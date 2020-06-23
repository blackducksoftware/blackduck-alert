package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public final class BlackDuckIssueTrackerCallbackUtility {
    public static final String COMPONENT_ISSUES_LINK_NAME = "component-issues";

    private final BlackDuckProviderKey blackDuckProviderKey;

    @Autowired
    public BlackDuckIssueTrackerCallbackUtility(BlackDuckProviderKey blackDuckProviderKey) {
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    public Optional<ComponentItemCallbackInfo> createCallbackInfo(NotificationType notificationType, ProjectVersionComponentView projectVersionComponentView) {
        return projectVersionComponentView.getFirstLink(COMPONENT_ISSUES_LINK_NAME)
                   .map(componentIssuesLink -> new ComponentItemCallbackInfo(componentIssuesLink, blackDuckProviderKey, notificationType.name()));
    }

}
