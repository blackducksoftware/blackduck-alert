package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtlassianTextContentNode implements AtlassianDocumentFormatNode, Serializable {
    public static final String NODE_TYPE = "text";
    private final String type;
    private final String text;
    private final List<AtlassianDocumentFormatNode> marks;

    public AtlassianTextContentNode(String text) {
        this(NODE_TYPE, text, new ArrayList<>());
    }

    protected AtlassianTextContentNode(String type, String text, List<AtlassianDocumentFormatNode> marks) {
        this.type = type;
        this.text = text;
        this.marks = marks;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Optional<List<AtlassianDocumentFormatNode>> getMarks() {
        if(marks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(marks);
    }

    public void addBoldStyle() {
        marks.add(new BoldMarkNode());
    }

    public void addLink(String href) {
        marks.add(new LinkMarkNode(href));
    }
}
