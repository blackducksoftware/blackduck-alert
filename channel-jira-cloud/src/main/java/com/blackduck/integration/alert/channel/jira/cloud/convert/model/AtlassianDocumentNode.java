package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AtlassianDocumentNode implements AtlassianDocumentFormatNode, Serializable {
    public static final String NODE_TYPE = "doc";
    public static final Integer DEFAULT_VERSION = 1;
    private final String type;
    private final Integer version;
    private final List<AtlassianParagraphContentNode> content;

    public AtlassianDocumentNode() {
        this(NODE_TYPE, DEFAULT_VERSION, new LinkedList<>());
    }

    protected AtlassianDocumentNode(String type, Integer version, List<AtlassianParagraphContentNode> content) {
        this.type = type;
        this.version = version;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public Integer getVersion() {
        return version;
    }

    public List<AtlassianParagraphContentNode> getContent() {
        return content;
    }

    public void addContent(AtlassianParagraphContentNode paragraphContentNode) {
        this.content.add(paragraphContentNode);
    }
}
