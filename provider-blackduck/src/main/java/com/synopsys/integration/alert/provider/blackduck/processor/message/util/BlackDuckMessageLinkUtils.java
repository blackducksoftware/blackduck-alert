/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;

public final class BlackDuckMessageLinkUtils {
    public static final String QUERY_PARAM_COMPONENT_NAME = "componentOrVersionName";
    public static final String URI_PIECE_COMPONENTS = "/components";
    public static final int URI_PIECE_COMPONENTS_LENGTH = URI_PIECE_COMPONENTS.length();

    public static String createProjectVersionComponentsLink(ProjectVersionComponentVersionView bomComponent) {
        String bomComponentUrl = bomComponent.getHref().toString();
        return createProjectVersionComponentsLink(bomComponentUrl);
    }

    public static String createComponentQueryLink(String bomComponentUrl, String bomComponentName) {
        String projectVersionComponentsLink = createProjectVersionComponentsLink(bomComponentUrl);
        return createComponentQueryLinkFromProjectVersionComponentsLink(projectVersionComponentsLink, bomComponentName);
    }

    public static String createComponentQueryLink(ProjectVersionComponentVersionView bomComponent) {
        String projectVersionComponentsLink = createProjectVersionComponentsLink(bomComponent);
        return createComponentQueryLinkFromProjectVersionComponentsLink(projectVersionComponentsLink, bomComponent.getComponentName());
    }

    private static String createProjectVersionComponentsLink(String bomComponentUrl) {
        int componentsStartIndex = StringUtils.lastIndexOf(bomComponentUrl, URI_PIECE_COMPONENTS);
        if (componentsStartIndex > 0) {
            return StringUtils.substring(bomComponentUrl, 0, componentsStartIndex + URI_PIECE_COMPONENTS_LENGTH);
        }
        return bomComponentUrl;
    }

    private static String createComponentQueryLinkFromProjectVersionComponentsLink(String projectVersionComponentsLink, String bomComponentName) {
        String encodedQueryValue = URLEncoder.encode(bomComponentName, StandardCharsets.UTF_8);
        // Encoding a space as a '+' is the standard (https://www.w3.org/TR/html4/references.html#ref-RFC1738). Black Duck does not decode the '+' as a space resulting in unsuccessful queries.
        String blackDuckEncodedQueryValue = encodedQueryValue.replace("+", "%20");
        return String.format("%s?q=%s:%s", projectVersionComponentsLink, QUERY_PARAM_COMPONENT_NAME, blackDuckEncodedQueryValue);
    }

    private BlackDuckMessageLinkUtils() {
    }

}
