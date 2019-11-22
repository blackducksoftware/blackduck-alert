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

import java.util.Collection;
import java.util.LinkedList;

public class IssueContentModel {
    private final String title;
    private final String description;
    // This member contains comments to add to the comments section of a ticket because the description is longer than the limit allowed by the issue tracker.
    private final Collection<String> descriptionComments;
    // These are comments to add to the issue when an existing issue is updated.
    private final Collection<String> additionalComments;

    private IssueContentModel(String title, String description, Collection<String> descriptionComments, Collection<String> additionalComments) {
        this.title = title;
        this.description = description;
        this.descriptionComments = descriptionComments;
        this.additionalComments = additionalComments;
    }

    public static final IssueContentModel of(String title, String description, Collection<String> descriptionComments) {
        return new IssueContentModel(title, description, descriptionComments, new LinkedList<>());
    }

    public static final IssueContentModel of(String title, String description, Collection<String> descriptionComments, Collection<String> additionalComments) {
        return new IssueContentModel(title, description, descriptionComments, additionalComments);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Collection<String> getDescriptionComments() {
        return descriptionComments;
    }

    public Collection<String> getAdditionalComments() {
        return additionalComments;
    }

}
