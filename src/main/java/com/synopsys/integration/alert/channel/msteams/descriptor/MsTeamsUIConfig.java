package com.synopsys.integration.alert.channel.msteams.descriptor;

import com.synopsys.integration.alert.channel.msteams.MsTeamsChannel;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;

public class MsTeamsUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_WEBHOOK = "Webhook";

    private static final String MSTEAMS_WEBHOOK_DESCRIPTION = "The MS Teams URL to receive alerts.";

    @Autowired
    public MsTeamsUIConfig(@Lazy final DescriptorMap descriptorMap) {
        super(MsTeamsChannel.COMPONENT_NAME, MsTeamsDescriptor.MSTEAMS_LABEL, MsTeamsDescriptor.MSTEAMS_URL, MsTeamsDescriptor.MSTEAMS_ICON, descriptorMap);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField webhook = TextInputConfigField.createRequired(MsTeamsDescriptor.KEY_WEBHOOK, LABEL_WEBHOOK, MSTEAMS_WEBHOOK_DESCRIPTION);
        return List.of(webhook);
    }

}
