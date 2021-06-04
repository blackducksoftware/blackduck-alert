/*
 * channel-slack
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class SlackGlobalUIConfig extends UIConfig {
    public SlackGlobalUIConfig() {
        super(SlackDescriptor.SLACK_LABEL, SlackDescriptor.SLACK_DESCRIPTION, SlackDescriptor.SLACK_URL);
    }

    @Override
    public List<ConfigField> createFields() {
        return List.of();
    }

}
