/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianBulletList;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianDocumentFormatNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianDocumentFormatRootNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianDocumentNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianListItem;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianParagraphContentNode;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianTextContentNode;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.jira.common.cloud.builder.AtlassianDocumentFormatModelBuilder;
import com.blackduck.integration.jira.common.cloud.model.AtlassianDocumentFormatModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;

/**
 * This class is used to build an {@link AtlassianDocumentFormatModel} object.
 * This class maintains state for the latest document and latest paragraph in order to be able to create a document
 * Once the document exceeds the size limit a new document is created and added to the list of documents for comment.
 * A new paragraph is also created in order to allow additional text nodes to be added.
 */
public class AtlassianDocumentBuilder {
    public static final Integer MAX_SERIALIZED_LENGTH = 30000;
    public static final String DESCRIPTION_CONTINUED_TEXT = "(description continued...)";
    public static final AtlassianTextContentNode descriptionContinuedNode = new AtlassianTextContentNode(DESCRIPTION_CONTINUED_TEXT);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper;
    private final ChannelMessageFormatter formatter;
    private final boolean descriptionDocument;

    // state variables
    private final AtlassianDocumentNode primaryNode;
    private final List<AtlassianDocumentNode> additionalCommentNodes;
    private AtlassianDocumentNode currentDocumentNode;
    private AtlassianParagraphContentNode currentParagraph;
    private AtlassianBulletList currentBulletList;

    private Integer currentDocumentLength;
    private Integer computedMaximumDocumentLength;

    public AtlassianDocumentBuilder(ChannelMessageFormatter formatter, boolean descriptionDocument) {
        this.objectMapper = new ObjectMapper();
        this.additionalCommentNodes = new ArrayList<>();
        this.formatter = formatter;

        // initialize an empty document with a single paragraph
        this.primaryNode = new AtlassianDocumentNode();
        this.currentDocumentNode = primaryNode;
        this.descriptionDocument = descriptionDocument;
        initializeMaximumDocumentLength();
        initializeDocumentLength();
        initializeNewParagraph();
    }

    // private methods to initialize new nodes in the document
    private void initializeMaximumDocumentLength() {
        Integer descriptionContinuedLength = computeJsonStringLength(DESCRIPTION_CONTINUED_TEXT);
        computedMaximumDocumentLength = MAX_SERIALIZED_LENGTH - descriptionContinuedLength;
    }

    private void initializeNewDocument() {
        this.currentDocumentNode = new AtlassianDocumentNode();
        initializeDocumentLength();
        additionalCommentNodes.add(this.currentDocumentNode);
    }

    private void initializeNewParagraph() {
        if (currentBulletList != null) {
            finishBulletList();
            startBulletList();
            addListItem();
        } else {
            this.currentParagraph = new AtlassianParagraphContentNode();
            this.currentDocumentNode.addContent(currentParagraph);
        }
    }

    private void initializeDocumentLength() {
        currentDocumentLength = computeJsonStringLength(currentDocumentNode);
    }

    private boolean willExceedLimit(Object object) {
        int newObjectLength = computeJsonStringLength(object);
        return this.currentDocumentLength + newObjectLength > computedMaximumDocumentLength;
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

    public AtlassianDocumentBuilder addParagraphNode() {
        AtlassianParagraphContentNode paragraphNode = new AtlassianParagraphContentNode();
        boolean willExceed = willExceedLimit(paragraphNode);
        if (willExceed) {
            initializeNewDocument();
        }

        this.currentParagraph = paragraphNode;
        this.currentDocumentNode.addContent(paragraphNode);
        if (willExceed) {
            addDescriptionContinuedText();
        }
        this.currentDocumentLength = computeJsonStringLength(currentDocumentNode);
        return this;
    }

    public AtlassianDocumentBuilder addTextNode(String text) {
        return addTextNode(text, false);
    }

    public AtlassianDocumentBuilder addTextNode(String text, boolean bold) {
        AtlassianTextContentNode textNode = new AtlassianTextContentNode(text);
        return addTextNode(textNode, bold, null);
    }

    public AtlassianDocumentBuilder addTextNode(LinkableItem linkableItem, boolean bold) {
        String label = formatter.encode(linkableItem.getLabel());
        String value = formatter.encode(linkableItem.getValue());
        String href = linkableItem.getUrl().map(formatter::encode).orElse(null);
        String text = String.format("%s:%s", label, formatter.getNonBreakingSpace());
        AtlassianTextContentNode textNode = new AtlassianTextContentNode(text);
        addTextNode(textNode, bold, null);
        textNode = new AtlassianTextContentNode(value);
        return addTextNode(textNode, bold, href);
    }

    public AtlassianDocumentBuilder addTextNode(String text, String href) {
        AtlassianTextContentNode textNode = new AtlassianTextContentNode(text);
        return addTextNode(textNode, false, href);
    }

    public AtlassianDocumentBuilder addTextNode(AtlassianTextContentNode textNode, boolean bold, @Nullable String href) {
        if (bold) {
            textNode.addBoldStyle();
        }

        if (StringUtils.isNotBlank(href)) {
            textNode.addLink(href);
        }

        if (willExceedLimit(textNode)) {
            initializeNewDocument();
            initializeNewParagraph();
            addDescriptionContinuedText();
        }

        this.currentParagraph.addContent(textNode);
        this.currentDocumentLength = computeJsonStringLength(currentDocumentNode);
        return this;
    }

    public AtlassianDocumentBuilder startBulletList() {
        currentBulletList = new AtlassianBulletList();

        if (willExceedLimit(currentBulletList)) {
            initializeNewDocument();
        }
        currentDocumentNode.addContent(currentBulletList);
        this.currentDocumentLength = computeJsonStringLength(currentDocumentNode);
        return this;
    }

    public AtlassianDocumentBuilder addListItem() {
        if (currentBulletList == null) {
            throw new IllegalStateException("Current bullet list is empty");
        }
        AtlassianListItem listItem = new AtlassianListItem();
        AtlassianParagraphContentNode paragraphNode = new AtlassianParagraphContentNode();
        listItem.addContent(paragraphNode);

        if (willExceedLimit(listItem)) {
            initializeNewDocument();
            currentDocumentNode.addContent(currentBulletList);
        }
        currentBulletList.addContent(listItem);
        this.currentParagraph = paragraphNode;
        this.currentDocumentLength = computeJsonStringLength(currentDocumentNode);
        return this;
    }

    public AtlassianDocumentBuilder finishBulletList() {
        if (currentBulletList == null) {
            throw new IllegalStateException("Current bullet list is empty");
        }
        currentBulletList = null;
        addParagraphNode();
        return this;
    }

    private void addDescriptionContinuedText() {
        if (descriptionDocument) {
            this.currentParagraph.addContent(descriptionContinuedNode);
        }
    }

    // This build will create a primary document for the i.e. for the description an if the document exceeds the limit then additional comments will need to be added.
    public AtlassianDocumentFormatModel buildPrimaryDocument() {
        return buildDocumentModel(primaryNode);
    }

    // This build will create a list of documents that are used to add comments to an issue because the content has exceeded the limit
    public List<AtlassianDocumentFormatModel> buildAdditionalCommentDocuments() {
        List<AtlassianDocumentFormatModel> documents = new ArrayList<>(additionalCommentNodes.size());

        for (AtlassianDocumentNode node : additionalCommentNodes) {
            documents.add(buildDocumentModel(node));
        }

        return documents;
    }

    // methods to convert the objects in this builder to the AtlassianDocumentFormatModel object
    private AtlassianDocumentFormatModel buildDocumentModel(AtlassianDocumentNode documentNode) {
        AtlassianDocumentFormatModelBuilder builder = new AtlassianDocumentFormatModelBuilder();

        documentNode.getContent().stream()
            .map(this::createRootContentNode)
            .forEach(nodeData -> builder.addContentNode(nodeData.getLeft(), nodeData.getRight()));
        return builder.build();
    }

    private Pair<String, List<Map<String, Object>>> createRootContentNode(AtlassianDocumentFormatNode rootNode) {
        if (rootNode instanceof final AtlassianDocumentFormatRootNode node) {
            String type = node.getType();
            List<Map<String, Object>> content = node.getContent().stream()
                .map(this::createContentNode)
                .toList();
            return Pair.of(type, content);
        }

        // should not get to this point.
        return Pair.of("", List.of());
    }

    private <T extends AtlassianDocumentFormatNode> Map<String, Object> createContentNode(T textContentNode) {
        return objectMapper.convertValue(
            textContentNode, new TypeReference<>() {
            }
        );
    }
}
