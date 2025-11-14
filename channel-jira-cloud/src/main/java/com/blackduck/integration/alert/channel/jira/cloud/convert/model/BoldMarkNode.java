/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;

import com.blackduck.integration.util.Stringable;

public class BoldMarkNode extends Stringable implements AtlassianDocumentFormatNode, Serializable {
    public static final String NODE_TYPE = "strong";
    private final String type;

    public BoldMarkNode() {
        this(NODE_TYPE);
    }

    protected BoldMarkNode(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
