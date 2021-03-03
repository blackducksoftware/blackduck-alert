/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public class MsTeamsUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_WEBHOOK = "Webhook";

    private static final String MSTEAMS_WEBHOOK_DESCRIPTION = "The MS Teams URL to receive alerts.";

    public MsTeamsUIConfig() {
        super(ChannelKey.MS_TEAMS, MsTeamsDescriptor.MSTEAMS_LABEL, MsTeamsDescriptor.MSTEAMS_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField webhook = new URLInputConfigField(MsTeamsDescriptor.KEY_WEBHOOK, LABEL_WEBHOOK, MSTEAMS_WEBHOOK_DESCRIPTION).applyRequired(true);
        return List.of(webhook);
    }

}
