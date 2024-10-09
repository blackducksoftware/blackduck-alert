/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.comment.model;

public class WorkItemCommentRequestModel {
    private String text;

    public WorkItemCommentRequestModel(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
