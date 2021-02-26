/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

/**
 * This lets the DescriptorProcessor create the appropriate ChannelDistributionTestAction/ConfigurationAction combination when no customization is needed.
 */
public interface AutoActionable {
    ChannelKey getChannelKey();

    DistributionChannel getChannel();
}
