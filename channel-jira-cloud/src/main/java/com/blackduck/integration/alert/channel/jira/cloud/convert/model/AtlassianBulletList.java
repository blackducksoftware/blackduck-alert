/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

public class AtlassianBulletList extends AtlassianRootNode {
    public static final String NODE_TYPE = "bulletList";

    public AtlassianBulletList() {
        super(NODE_TYPE);
    }
}
