package com.synopsys.integration.alert.descriptor.api;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.RemediatorChannelKey;

@Component
public class GitHubChannelKey extends ChannelKey implements RemediatorChannelKey {
    private static final String COMPONENT_NAME = "channel_github";
    private static final String GITHUB_DISPLAY_NAME = "GitHub";

    public GitHubChannelKey() {
        super(COMPONENT_NAME, GITHUB_DISPLAY_NAME);
    }
}
