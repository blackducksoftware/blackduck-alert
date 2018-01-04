package com.blackducksoftware.integration.hub.alert;

public enum TestPropertyKey {
    TEST_HUB_SERVER_URL("blackduck.hub.url"),
    TEST_HUB_PORT("blackduck.hub.port"),
    TEST_USERNAME("blackduck.hub.username"),
    TEST_PASSWORD("blackduck.hub.password"),
    TEST_TRUST_HTTPS_CERT("blackduck.hub.trust.cert"),
    TEST_HUB_TIMEOUT("blackduck.hub.timeout"),
    TEST_CRON("alert.accumulator.cron"),
    TEST_DAILY_DIGEST("alert.digest.daily.cron"),
    TEST_REALTIME_DIGEST("alert.digest.realtime.cron"),
    TEST_HIPCHAT_ROOM_ID("hipchat.room.id"),
    TEST_HIPCHAT_API_KEY("hipchat.api.key"),
    TEST_SLACK_CHANNEL_NAME("slack.channel.name"),
    TEST_SLACK_USERNAME("slack.username"),
    TEST_SLACK_WEBHOOK("slack.web.hook"),
    TEST_EMAIL_RECIPIENT("mail.recipient"),
    TEST_EMAIL_SMTP_HOST("mail.smtp.host"),
    TEST_EMAIL_SMTP_FROM("mail.smtp.from"),
    TEST_EMAIL_TEMPLATE("hub.email.template.directory"),
    TEST_EMAIL_LOGO("logo.image");

    private final String propertyKey;

    private TestPropertyKey(final String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return this.propertyKey;
    }
}
