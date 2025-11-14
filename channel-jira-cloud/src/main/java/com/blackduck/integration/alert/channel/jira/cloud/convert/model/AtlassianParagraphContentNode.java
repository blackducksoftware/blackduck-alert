package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.util.LinkedList;

public class AtlassianParagraphContentNode extends AtlassianRootNode {
    public static final String NODE_TYPE = "paragraph";

    public AtlassianParagraphContentNode() {
        super(NODE_TYPE, new LinkedList<>());
    }
}
