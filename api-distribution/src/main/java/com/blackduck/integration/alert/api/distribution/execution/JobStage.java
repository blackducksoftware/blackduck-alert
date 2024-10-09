/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.execution;

public enum JobStage {
    NOTIFICATION_PROCESSING,
    CHANNEL_PROCESSING,
    ISSUE_CREATION,
    ISSUE_COMMENTING,
    ISSUE_TRANSITION
}
