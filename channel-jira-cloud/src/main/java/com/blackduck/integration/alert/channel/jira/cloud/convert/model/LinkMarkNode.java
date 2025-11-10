package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;

public class LinkMarkNode implements AtlassianDocumentFormatNode, Serializable {
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
