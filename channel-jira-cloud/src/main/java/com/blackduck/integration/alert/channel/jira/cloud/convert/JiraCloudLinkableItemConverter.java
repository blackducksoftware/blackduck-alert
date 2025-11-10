package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.jira.common.cloud.builder.AtlassianDocumentFormatModelBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JiraCloudLinkableItemConverter {
    private final ChannelMessageFormatter formatter;

    public JiraCloudLinkableItemConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public Pair<String, List<Map<String, Object>>> convertToString(LinkableItem linkableItem, boolean bold) {
        return convertToString(
                formatter.encode(linkableItem.getLabel()),
                formatter.encode(linkableItem.getValue()),
                linkableItem.getUrl().map(formatter::encode).orElse(null),
                bold
        );
    }

    public Pair<String, List<Map<String, Object>>> convertToStringWithoutLink(LinkableItem linkableItem, boolean bold) {
        return convertToString(
                formatter.encode(linkableItem.getLabel()),
                formatter.encode(linkableItem.getValue()),
                null,
                bold
        );
    }

    private Pair<String, List<Map<String, Object>>> convertToString(String encodedLabel, String encodedValue, @Nullable String encodedUrl, boolean bold) {
        Map<String,Object> nodeContent = new LinkedHashMap<>();
        AtlassianDocumentFormatUtil.createTextNode(String.format("%s:%s%s",encodedLabel, formatter.getNonBreakingSpace(), encodedValue));

        // to style the text need to create a marks json node content that looks like this:
        // "marks": [
        //    {
        //      "type": "strong"
        //    },
        //    {
        //      "type": "link",
        //      "attrs": {
        //        "href": "https://www.example.com"
        //      }
        //    }
        //  ]

        if (bold) {
            AtlassianDocumentFormatUtil.addBoldStylingToNode(nodeContent);
        }

        if (null != encodedUrl) {
            AtlassianDocumentFormatUtil.addUrlToNode(nodeContent, encodedUrl);
        }

        return Pair.of(AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_TYPE_PARAGRAPH, List.of(nodeContent));
    }
}
