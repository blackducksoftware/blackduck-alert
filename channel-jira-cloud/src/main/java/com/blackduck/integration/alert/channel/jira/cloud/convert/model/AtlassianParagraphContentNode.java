/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.util.LinkedList;

public class AtlassianParagraphContentNode extends AtlassianRootNode {
    public static final String NODE_TYPE = "paragraph";

    public AtlassianParagraphContentNode() {
        super(NODE_TYPE, new LinkedList<>());
    }
}
