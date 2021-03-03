/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerContentLengthException;

public class IssueContentLengthValidator {
    private final int titleLength;
    private final int descriptionLength;
    private final int commentLength;

    public IssueContentLengthValidator(int titleLength, int descriptionLength, int commentLength) {
        this.titleLength = titleLength;
        this.descriptionLength = descriptionLength;
        this.commentLength = commentLength;
    }

    public boolean validateContentLength(IssueContentModel issueContent) throws IssueTrackerContentLengthException {
        StringBuilder errors = new StringBuilder();
        int titleLength = StringUtils.length(issueContent.getTitle());
        int descriptionLength = StringUtils.length(issueContent.getDescription());

        if (titleLength > getTitleLength()) {
            errors.append(String.format("Title longer than the limit of %d characters. ", getTitleLength()));
        }

        if (descriptionLength > getDescriptionLength()) {
            errors.append(String.format("Description longer than the limit of %d characters. ", getTitleLength()));
        }

        Predicate<String> commentLengthTest = comment -> StringUtils.length(comment) > getCommentLength();
        boolean descriptorCommentsTooLong = issueContent.getDescriptionComments().stream()
                                                .anyMatch(commentLengthTest);
        if (descriptorCommentsTooLong) {
            errors.append(String.format("One of the comments is longer than the limit of %d characters. ", getCommentLength()));
        }

        boolean additionalCommentsTooLong = issueContent.getAdditionalComments().stream()
                                                .anyMatch(commentLengthTest);
        if (additionalCommentsTooLong) {
            errors.append(String.format("One of the comments is longer than the limit of %d characters. ", getCommentLength()));
        }

        if (errors.length() > 0) {
            throw new IssueTrackerContentLengthException(errors.toString());
        }

        return true;
    }

    public int getTitleLength() {
        return titleLength;
    }

    public int getDescriptionLength() {
        return descriptionLength;
    }

    public int getCommentLength() {
        return commentLength;
    }

}
