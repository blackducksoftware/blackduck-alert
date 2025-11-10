package com.blackduck.integration.alert.channel.jira.cloud.convert;

import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianDocumentFormatNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianDocumentNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianParagraphContentNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianTextContentNode;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AtlassianDocumentBuilder {
    public static final Integer MAX_SERIALIZED_LENGTH = 30000;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper;

    private AtlassianDocumentNode primaryNode;
    private List<AtlassianDocumentNode> additionalCommentNodes;
    private AtlassianDocumentNode currentDocumentNode;
    private AtlassianParagraphContentNode currentParagraph;
    private Integer currentDocumentLength;

    public AtlassianDocumentBuilder() {
        this.objectMapper = new ObjectMapper();
        this.additionalCommentNodes = new ArrayList<>();
        this.primaryNode = new AtlassianDocumentNode();
        this.currentDocumentNode = primaryNode;
        initializeDocumentLength();
    }

    private void initialzeNewDocument() {
        this.currentDocumentNode = new AtlassianDocumentNode();
        initializeDocumentLength();
        additionalCommentNodes.add(this.currentDocumentNode);
    }

    private void initializeNewParagraph() {
        this.currentParagraph = new AtlassianParagraphContentNode();
        this.currentDocumentNode.addContent(currentParagraph);
    }

    private void initializeDocumentLength() {
        currentDocumentLength = computeJsonStringLength(currentDocumentNode);
    }

    private boolean willExceedLimit(Object object) {
        int newObjectLength = computeJsonStringLength(object);
        return (this.currentDocumentLength + newObjectLength) >  MAX_SERIALIZED_LENGTH;
    }

    private int computeJsonStringLength(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return json.length();
        } catch (JsonProcessingException ex) {
            logger.error("Error while initializing document length", ex);
        }
        return 0;
    }

    public AtlassianDocumentBuilder addParagraphNode(AtlassianParagraphContentNode paragraphNode) {
        if(willExceedLimit(paragraphNode)) {
            initialzeNewDocument();
        }

        this.currentParagraph = paragraphNode;
        this.currentDocumentNode.addContent(paragraphNode);
        return this;
    }

    public AtlassianDocumentBuilder addAdditionalCommentNode(AtlassianTextContentNode textNode, boolean bold, String href) {
        if(bold) {
            textNode.addBoldStyle();
        }

        if(StringUtils.isNotBlank(href)) {
            textNode.addLink(href);
        }

        if(willExceedLimit(textNode)) {
            initialzeNewDocument();
            initializeNewParagraph();
        }

        this.currentParagraph.addContent(textNode);
        return this;
    }

//    public AtlassianDocumentFormatModel buildPrimaryDocument() {
//
//    }
//
//    public List<AtlassianDocumentFormatModel> buildAdditionalCommentDocuments() {
//
//    }
}
