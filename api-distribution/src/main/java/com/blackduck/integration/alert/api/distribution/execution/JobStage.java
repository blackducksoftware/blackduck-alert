package com.blackduck.integration.alert.api.distribution.execution;

public enum JobStage {
    NOTIFICATION_PROCESSING,
    CHANNEL_PROCESSING,
    ISSUE_CREATION,
    ISSUE_COMMENTING,
    ISSUE_TRANSITION
}
