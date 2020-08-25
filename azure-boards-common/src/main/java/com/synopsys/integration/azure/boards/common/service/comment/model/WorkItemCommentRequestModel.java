package com.synopsys.integration.azure.boards.common.service.comment.model;

public class WorkItemCommentRequestModel {
    private String text;

    public WorkItemCommentRequestModel(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
