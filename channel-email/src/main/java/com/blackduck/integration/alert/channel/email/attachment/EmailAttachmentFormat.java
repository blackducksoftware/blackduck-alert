/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.attachment;

public enum EmailAttachmentFormat {
    NONE,
    JSON,
    XML,
    CSV;

    public static EmailAttachmentFormat getValueSafely(String name) {
        try {
            return EmailAttachmentFormat.valueOf(name);
        } catch (Exception e) {
            return NONE;
        }
    }

}
