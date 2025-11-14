package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AtlassianRootNode implements AtlassianDocumentFormatRootNode, Serializable {
    private final String type;
    private final List<AtlassianDocumentFormatNode> content;

    public AtlassianRootNode(String type) {
        this(type, new LinkedList<>());
    }

    public AtlassianRootNode(final String type, final List<AtlassianDocumentFormatNode> content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public List<AtlassianDocumentFormatNode> getContent() {
        return content;
    }

    @Override
    public void addContent(final AtlassianDocumentFormatNode content) {
        this.content.add(content);
    }
}
