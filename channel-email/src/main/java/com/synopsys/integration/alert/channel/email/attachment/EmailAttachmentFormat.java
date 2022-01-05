/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment;

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
