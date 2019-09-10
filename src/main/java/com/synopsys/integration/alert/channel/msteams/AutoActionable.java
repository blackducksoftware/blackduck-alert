package com.synopsys.integration.alert.channel.msteams;

import com.synopsys.integration.alert.common.channel.ChannelKey;
import com.synopsys.integration.alert.common.channel.DistributionChannel;

/**
 * This lets the DescriptorProcessor create the appropriate ChannelDistributionTestAction/ConfigurationAction combination when no customization is needed.
 */
public interface AutoActionable {
    ChannelKey getChannelKey();

    DistributionChannel getChannel();

}
