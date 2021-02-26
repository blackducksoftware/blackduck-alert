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
    private final int titleLengthLimit;
    private final int descriptionLengthLimit;
    private final int commentLengthLimit;

    public IssueContentLengthValidator(int titleLengthLimit, int descriptionLengthLimit, int commentLengthLimit) {
        this.titleLengthLimit = titleLengthLimit;
        this.descriptionLengthLimit = descriptionLengthLimit;
        this.commentLengthLimit = commentLengthLimit;
    }

    public boolean validateContentLength(IssueContentModel issueContent) throws IssueTrackerContentLengthException {
        StringBuilder errors = new StringBuilder();
        int issueContentTitleLength = StringUtils.length(issueContent.getTitle());
        int issueContentDescriptionLength = StringUtils.length(issueContent.getDescription());

        if (issueContentTitleLength > getTitleLengthLimit()) {
            errors.append(String.format("Title longer than the limit of %d characters. ", getTitleLengthLimit()));
        }

        if (issueContentDescriptionLength > getDescriptionLengthLimit()) {
            errors.append(String.format("Description longer than the limit of %d characters. ", getDescriptionLengthLimit()));
        }

        Predicate<String> commentLengthTest = comment -> StringUtils.length(comment) > getCommentLengthLimit();
        boolean descriptorCommentsTooLong = issueContent.getDescriptionComments().stream()
                                                .anyMatch(commentLengthTest);
        if (descriptorCommentsTooLong) {
            errors.append(String.format("One of the comments is longer than the limit of %d characters. ", getCommentLengthLimit()));
        }

        boolean additionalCommentsTooLong = issueContent.getAdditionalComments().stream()
                                                .anyMatch(commentLengthTest);
        if (additionalCommentsTooLong) {
            errors.append(String.format("One of the comments is longer than the limit of %d characters. ", getCommentLengthLimit()));
        }

        if (errors.length() > 0) {
            throw new IssueTrackerContentLengthException(errors.toString());
        }

        return true;
    }

    public int getTitleLengthLimit() {
        return titleLengthLimit;
    }

    public int getDescriptionLengthLimit() {
        return descriptionLengthLimit;
    }

    public int getCommentLengthLimit() {
        return commentLengthLimit;
    }

}
