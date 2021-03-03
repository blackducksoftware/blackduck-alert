/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.builder;

import java.util.List;
import java.util.function.Consumer;

import com.synopsys.integration.alert.common.message.model.CommonMessageData;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public abstract class BlackDuckMessageBuilder<T> {
    private final String providerName = "Black Duck";
    private final NotificationType notificationType;

    public BlackDuckMessageBuilder(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getProviderName() {
        return this.providerName;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public abstract List<ProviderMessageContent> buildMessageContents(CommonMessageData commonMessageData, T notificationView, BlackDuckServicesFactory blackDuckServicesFactory);

    protected String getNullableProjectUrlFromProjectVersion(String projectVersionURL, BlackDuckApiClient blackDuckService, Consumer<String> logMethod) {
        String projectURL = null;
        try {
            ProjectVersionView projectVersionView = blackDuckService.getResponse(new HttpUrl(projectVersionURL), ProjectVersionView.class);

            projectURL = projectVersionView.getFirstLinkSafely(ProjectVersionView.PROJECT_LINK).map(HttpUrl::string).orElse(null);
        } catch (IntegrationException e) {
            logMethod.accept(String.format("Could not get the Project Version for '%s': %s", projectVersionURL, e.getMessage()));
            return projectURL;
        }
        return projectURL;
    }

}
