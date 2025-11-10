package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AtlassianParagraphContentNode implements AtlassianDocumentFormatNode, Serializable {
    public static final String NODE_TYPE = "paragraph";
    private final String type;
    private final List<AtlassianTextContentNode> content;

    public AtlassianParagraphContentNode() {
        this(NODE_TYPE, new LinkedList<>());
    }

    protected AtlassianParagraphContentNode(String type, List<AtlassianTextContentNode> content) {
        this.type = type;
        this.content = content;
    }

    public List<AtlassianTextContentNode> getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public void addContent(AtlassianTextContentNode textContentNode) {
        this.content.add(textContentNode);
    }
}
