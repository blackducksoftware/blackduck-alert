/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.Collection;
import java.util.LinkedList;

public class IssueContentModel {
    private final String title;
    private final String description;
    private final Collection<String> descriptionComments;
    private final Collection<String> additionalComments;

    /**
     * Model constructor.
     * @param title               The title of the issue.  This may be abbreviated.
     * @param description         The description of the issue.  This may have size limits associated.  If the size limit is exceeded then any additional text should be in the descriptionComments.
     * @param descriptionComments Contains comments to add to the comments section of a ticket because the description is longer than the limit allowed by the issue tracker.
     * @param additionalComments  Comments to add to the issue when an existing issue is updated.
     */
    private IssueContentModel(String title, String description, Collection<String> descriptionComments, Collection<String> additionalComments) {
        this.title = title;
        this.description = description;
        this.descriptionComments = descriptionComments;
        this.additionalComments = additionalComments;
    }

    public static IssueContentModel of(String title, String description, Collection<String> descriptionComments) {
        return new IssueContentModel(title, description, descriptionComments, new LinkedList<>());
    }

    public static IssueContentModel of(String title, String description, Collection<String> descriptionComments, Collection<String> additionalComments) {
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
