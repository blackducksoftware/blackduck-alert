/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;

import com.blackduck.integration.util.Stringable;

public class LinkMarkNode extends Stringable implements AtlassianDocumentFormatNode, Serializable {
    public static final String NODE_TYPE = "link";
    private final String type;
    private final HrefNode attrs;

    public LinkMarkNode(String href) {
        this(NODE_TYPE, new HrefNode(href));
    }

    protected LinkMarkNode(String type, HrefNode attrs) {
        this.type = type;
        this.attrs = attrs;
    }

    @Override
    public String getType() {
        return type;
    }

    public HrefNode getAttrs() {
        return attrs;
    }
}
