/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.util.List;

public interface AtlassianDocumentFormatRootNode extends AtlassianDocumentFormatNode {
    List<AtlassianDocumentFormatNode> getContent();

    void addContent(AtlassianDocumentFormatNode content);
}
