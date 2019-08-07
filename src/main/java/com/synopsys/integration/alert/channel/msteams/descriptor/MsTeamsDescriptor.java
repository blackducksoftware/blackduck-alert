package com.synopsys.integration.alert.channel.msteams.descriptor;

import com.synopsys.integration.alert.channel.msteams.MsTeamsChannel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

public class MsTeamsDescriptor extends ChannelDescriptor {
    public static final String KEY_WEBHOOK = "channel.msteams.webhook";

    public static final String MSTEAMS_LABEL = "MS Teams";
    public static final String MSTEAMS_URL = "msteams";
    public static final String MSTEAMS_ICON = "fab/msteams";

    @Autowired
    public MsTeamsDescriptor(final MsTeamsUIConfig msTeamsUIConfig) {
        super(MsTeamsChannel.COMPONENT_NAME, msTeamsUIConfig);
    }

}
