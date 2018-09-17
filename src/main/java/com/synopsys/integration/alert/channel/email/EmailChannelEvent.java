package com.synopsys.integration.alert.channel.email;

import java.util.Set;

import com.synopsys.integration.alert.channel.event.ChannelEvent;

public class EmailChannelEvent extends ChannelEvent {
    private final Set<String> emailAddresses;
    private final String subjectLine;

    public EmailChannelEvent(final String createdAt, final String provider, final String notificationType, final String content, final Long notificationId, final Long commonConfigId, final Set<String> emailAddresses,
        final String subjectLine) {
        super(EmailGroupChannel.COMPONENT_NAME, createdAt, provider, notificationType, content, notificationId, commonConfigId);
        this.emailAddresses = emailAddresses;
        this.subjectLine = subjectLine;
    }

    public Set<String> getEmailAddresses() {
        return emailAddresses;
    }

    public String getSubjectLine() {
        return subjectLine;
    }

}
