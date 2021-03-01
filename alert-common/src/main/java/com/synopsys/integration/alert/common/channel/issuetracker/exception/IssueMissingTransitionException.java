/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.exception;

import java.util.List;

public class IssueMissingTransitionException extends IssueTrackerException {
    private final String issueKey;
    private final String missingTransition;
    private final List<String> validTransitions;

    public IssueMissingTransitionException(String issueKey, String missingTransition, List<String> validTransitions) {
        this.issueKey = issueKey;
        this.missingTransition = missingTransition;
        this.validTransitions = validTransitions;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getMissingTransition() {
        return missingTransition;
    }

    public List<String> getValidTransitions() {
        return validTransitions;
    }

}
