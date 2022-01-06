/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.exception;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class IssueMissingTransitionException extends IssueTrackerException {
    private final String issueKey;
    private final String missingTransition;
    private final List<String> validTransitions;

    public IssueMissingTransitionException(String issueKey, String missingTransition, List<String> validTransitions) {
        super(createExceptionMessage(issueKey, missingTransition, validTransitions));
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

    private static String createExceptionMessage(String issueKey, String missingTransition, List<String> validTransitions) {
        String exceptionMessage = String.format("The transition '%s' was missing for issue with key '%s'.", missingTransition, issueKey);
        if (!validTransitions.isEmpty()) {
            String joinedValidTransitions = StringUtils.join(validTransitions, ", ");
            return String.format("%s The valid transitions from the issue's current state are: %s", exceptionMessage, joinedValidTransitions);
        }
        return exceptionMessage;
    }

}
