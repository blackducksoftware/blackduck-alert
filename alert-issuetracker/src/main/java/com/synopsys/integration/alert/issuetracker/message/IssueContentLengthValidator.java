/**
 * alert-issuetracker
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
