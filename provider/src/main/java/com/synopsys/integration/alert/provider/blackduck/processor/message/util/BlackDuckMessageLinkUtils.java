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

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;

public final class BlackDuckMessageLinkUtils {
    public static final String URI_PIECE_COMPONENTS = "/components";
    public static final int URI_PIECE_COMPONENTS_LENGTH = URI_PIECE_COMPONENTS.length();

    public static String createProjectVersionComponentsLink(ProjectVersionComponentView bomComponent) {
        String bomComponentUrl = bomComponent.getHref().toString();
        int componentsStartIndex = StringUtils.lastIndexOf(bomComponentUrl, URI_PIECE_COMPONENTS);
        if (componentsStartIndex > 0) {
            return StringUtils.substring(bomComponentUrl, 0, componentsStartIndex + URI_PIECE_COMPONENTS_LENGTH);
        }
        return bomComponentUrl;
    }

    public static String createComponentQueryLink(ProjectVersionComponentView bomComponent) {
        String projectVersionComponentsLink = createProjectVersionComponentsLink(bomComponent);
        return String.format("%s?q=componentOrVersionName:%s", projectVersionComponentsLink, bomComponent.getComponentName());
    }

    private BlackDuckMessageLinkUtils() {
    }

}
