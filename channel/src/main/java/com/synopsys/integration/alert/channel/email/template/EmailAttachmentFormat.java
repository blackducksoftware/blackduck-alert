/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.template;

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
