package com.blackduck.integration.alert.channel.jira.cloud.convert.model;

import java.util.List;

public interface AtlassianDocumentFormatRootNode extends AtlassianDocumentFormatNode {
    List<AtlassianDocumentFormatNode> getContent();

    void addContent(AtlassianDocumentFormatNode content);
}
