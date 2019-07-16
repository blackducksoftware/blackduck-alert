package com.synopsys.integration.alert.common.rest.model;

import java.util.Map;

public class CustomMessageFieldModel extends FieldModel {
    private String messageContent;

    public CustomMessageFieldModel(final String descriptorName, final String context, final Map<String, FieldValueModel> keyToValues, final String messageContent) {
        super(descriptorName, context, keyToValues);
        this.messageContent = messageContent;
    }

    public CustomMessageFieldModel(final String configId, final String descriptorName, final String context, final Map<String, FieldValueModel> keyToValues, final String messageContent) {
        super(configId, descriptorName, context, keyToValues);
        this.messageContent = messageContent;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(final String messageContent) {
        this.messageContent = messageContent;
    }

}
