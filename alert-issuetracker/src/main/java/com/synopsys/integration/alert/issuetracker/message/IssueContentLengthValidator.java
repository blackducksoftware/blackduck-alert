package com.synopsys.integration.alert.issuetracker.message;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerContentFormatException;

public abstract class IssueContentLengthValidator {

    public abstract int getTitleLength();

    public abstract int getDescriptionLength();

    public abstract int getCommentLength();

    public boolean validateContentLength(IssueContentModel issueContent) throws IssueTrackerContentFormatException {
        StringBuilder errors = new StringBuilder();
        int titleLength = StringUtils.length(issueContent.getTitle());
        int descriptionLength = StringUtils.length(issueContent.getDescription());

        if (titleLength > getTitleLength()) {
            errors.append(String.format("Title longer than the limit of %d characters. ", getTitleLength()));
        }

        if (descriptionLength > getDescriptionLength()) {
            errors.append(String.format("Description longer than the limit of %d characters. ", getTitleLength()));
        }

        boolean commentTooLong = issueContent.getAdditionalComments().stream()
                                     .anyMatch(comment -> StringUtils.length(comment) > getCommentLength());
        if (commentTooLong) {
            errors.append(String.format("One of the comments is longer than the limit of %d characters. ", getCommentLength()));
        }

        if (errors.length() > 0) {
            throw new IssueTrackerContentFormatException(errors.toString());
        }

        return true;
    }
}
