/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.util.LinkedList;
import java.util.List;

public class AtlassianDocumentNode extends AtlassianRootNode {
    public static final String NODE_TYPE = "doc";
    public static final Integer DEFAULT_VERSION = 1;
    private final Integer version;

    public AtlassianDocumentNode() {
        this(NODE_TYPE, DEFAULT_VERSION, new LinkedList<>());
    }

    protected AtlassianDocumentNode(String type, Integer version, List<AtlassianDocumentFormatNode> content) {
        super(type, content);
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }
}
