package com.blackduck.integration.alert.api.descriptor;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;

@Component
public final class EmailChannelKey extends ChannelKey {
    private static final String COMPONENT_NAME = "channel_email";
    private static final String EMAIL_DISPLAY_NAME = "Email";

    public EmailChannelKey() {
        super(COMPONENT_NAME, EMAIL_DISPLAY_NAME);
    }

}
