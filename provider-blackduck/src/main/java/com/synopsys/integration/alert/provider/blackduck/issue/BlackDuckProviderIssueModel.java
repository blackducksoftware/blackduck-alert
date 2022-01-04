/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.issue;

public class BlackDuckProviderIssueModel {
    public static final String DEFAULT_ASSIGNEE = "Alert User";

    private final String key;
    private final String status;
    private final String summary;
    private final String link;

    private String assignee = DEFAULT_ASSIGNEE;

    public BlackDuckProviderIssueModel(String key, String status, String summary, String link) {
        this.key = key;
        this.status = status;
        this.summary = summary;
        this.link = link;
    }

    public String getKey() {
        return key;
    }

    public String getStatus() {
        return status;
    }

    public String getSummary() {
        return summary;
    }

    public String getLink() {
        return link;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

}
