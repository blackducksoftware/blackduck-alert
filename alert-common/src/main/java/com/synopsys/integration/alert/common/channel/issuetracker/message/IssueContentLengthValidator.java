/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerContentLengthException;

public abstract class IssueContentLengthValidator {
    protected abstract int getTitleLength();

    protected abstract int getDescriptionLength();

    protected abstract int getCommentLength();

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

}
