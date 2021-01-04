/**
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
