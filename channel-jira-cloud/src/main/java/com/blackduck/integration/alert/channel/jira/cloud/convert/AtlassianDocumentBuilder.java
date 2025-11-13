package com.blackduck.integration.alert.channel.jira.cloud.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.channel.jira.cloud.convert.model.AtlassianDocumentNode;
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
    private Integer currentDocumentLength;
    private Integer descriptionContinuedLength;

    public AtlassianDocumentBuilder(ChannelMessageFormatter formatter, boolean descriptionDocument) {
        this.objectMapper = new ObjectMapper();
        this.additionalCommentNodes = new ArrayList<>();
        this.formatter = formatter;

        // initialize an empty document with a single paragraph
        this.primaryNode = new AtlassianDocumentNode();
        this.currentDocumentNode = primaryNode;
        this.descriptionDocument = descriptionDocument;
        initializeDescriptionContinuedTextNodeLength();
        initializeDocumentLength();
        initializeNewParagraph();
    }

    // private methods to initialize new nodes in the document
    private void initializeDescriptionContinuedTextNodeLength() {
        descriptionContinuedLength = computeJsonStringLength(DESCRIPTION_CONTINUED_TEXT);
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
        return (this.currentDocumentLength + newObjectLength) > (MAX_SERIALIZED_LENGTH - descriptionContinuedLength);
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
            initialzeNewDocument();
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
            initialzeNewDocument();
            initializeNewParagraph();
            addDescriptionContinuedText();
        }

        this.currentParagraph.addContent(textNode);
        this.currentDocumentLength = computeJsonStringLength(currentDocumentNode);
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
            .map(this::createParagraphContentNode)
            .forEach(nodeData -> builder.addContentNode(AtlassianDocumentFormatModelBuilder.DOCUMENT_NODE_TYPE_PARAGRAPH, nodeData));
        return builder.build();
    }

    private List<Map<String, Object>> createParagraphContentNode(AtlassianParagraphContentNode paragraphNode) {
        return paragraphNode.getContent().stream()
            .map(this::createTextContentNode)
            .toList();
    }

    private Map<String, Object> createTextContentNode(AtlassianTextContentNode textContentNode) {
        return objectMapper.convertValue(
            textContentNode, new TypeReference<>() {
            }
        );
    }
}
