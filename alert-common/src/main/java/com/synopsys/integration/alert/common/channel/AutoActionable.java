package com.synopsys.integration.alert.common.channel;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public interface AutoActionable {
    ChannelKey getChannelKey();

    DistributionChannel getChannel();
}
