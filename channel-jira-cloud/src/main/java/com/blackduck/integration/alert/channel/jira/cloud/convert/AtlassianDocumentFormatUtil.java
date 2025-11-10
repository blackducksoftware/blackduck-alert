package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.jira.common.cloud.builder.AtlassianDocumentFormatModelBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AtlassianDocumentFormatUtil {

    public static final String DOCUMENT_NODE_MARKS = "marks";
    public static final String DOCUMENT_NODE_MARK_TYPE_EMPHASIZE = "strong";
    public static final String DOCUMENT_NODE_MARK_TYPE_LINK = "link";
    public static final String DOCUMENT_NODE_MARK_ATTRIBUTE = "attrs";
    public static final String DOCUMENT_NODE_MARK_ATTRIBUTE_HREF = "href";

    public static Map<String, Object> createTextNode(String text) {
        Map<String,Object> nodeContent = new LinkedHashMap<>();
        nodeContent.put(AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_ATTRIBUTE_TYPE, AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_TYPE_TEXT);
        nodeContent.put(AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_ATTRIBUTE_TEXT,text);

        return nodeContent;
    }

    public static List<Map<String,Object>> getOrAddMarksNode(Map<String, Object> contentNode) {
        if(contentNode == null) {
            return new ArrayList<>();
        }
        List<Map<String,Object>> marksContent;
        if(contentNode.containsKey(DOCUMENT_NODE_MARKS)) {
            marksContent = (List<Map<String, Object>>) contentNode.get(DOCUMENT_NODE_MARKS);
        } else {
            marksContent = new ArrayList<>();
            contentNode.put(DOCUMENT_NODE_MARKS, marksContent);
        }

        return marksContent;
    }

    public static void addBoldStylingToNode(Map<String,Object> contentNode) {
        // Add node to marks list;
        // "marks": [
        //    {
        //      "type": "strong"
        //    },
        //  ]
        // do nothing
        if(contentNode == null) {
            return;
        }
        List<Map<String,Object>> marksContent = getOrAddMarksNode(contentNode);

        Map<String, Object> boldContent = new HashMap<>();
        boldContent.put(AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_ATTRIBUTE_TYPE, DOCUMENT_NODE_MARK_TYPE_EMPHASIZE);
        marksContent.add(boldContent);
    }

    public static void addUrlToNode(Map<String,Object> contentNode, String url) {
        //  create the marks node to represent a link:
        //    {
        //      "type": "link",
        //      "attrs": {
        //        "href": "https://www.example.com"
        //      }
        //    }
        // do nothing
        if(contentNode == null) {
            return;
        }
        List<Map<String,Object>> marksContent = getOrAddMarksNode(contentNode);

        Map<String,Object> urlMarksContent = new LinkedHashMap<>();
        urlMarksContent.put(AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_ATTRIBUTE_TYPE, DOCUMENT_NODE_MARK_TYPE_LINK);
        urlMarksContent.put(DOCUMENT_NODE_MARK_ATTRIBUTE, Map.of(DOCUMENT_NODE_MARK_ATTRIBUTE_HREF, url));
        marksContent.add(urlMarksContent);
    }

    private AtlassianDocumentFormatUtil() {
        // prevent instantiation.
    }
}
