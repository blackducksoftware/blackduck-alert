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
