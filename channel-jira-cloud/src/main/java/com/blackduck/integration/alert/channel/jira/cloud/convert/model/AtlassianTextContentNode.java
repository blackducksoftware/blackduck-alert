/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class AtlassianTextContentNode implements AtlassianDocumentFormatNode, Serializable {
    public static final String NODE_TYPE = "text";
    private final String type;
    private final String text;
    private final Set<AtlassianDocumentFormatNode> marks;

    public AtlassianTextContentNode(String text) {
        this(NODE_TYPE, text, new LinkedHashSet<>());
    }

    protected AtlassianTextContentNode(String type, String text, Set<AtlassianDocumentFormatNode> marks) {
        this.type = type;
        this.text = text;
        this.marks = marks == null ? new LinkedHashSet<>() : marks;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Nullable
    public Set<AtlassianDocumentFormatNode> getMarks() {
        if (marks.isEmpty()) {
            return null;
        }
        return marks;
    }

    public void addBoldStyle() {
        marks.add(new BoldMarkNode());
    }

    public void addLink(String href) {
        marks.add(new LinkMarkNode(href));
    }
}
