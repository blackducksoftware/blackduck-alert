/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public abstract class ChannelDescriptor extends Descriptor {
    public ChannelDescriptor(ChannelKey channelKey, ChannelDistributionUIConfig distributionUIConfig, UIConfig globalUIConfig) {
        super(channelKey, DescriptorType.CHANNEL);
        addDistributionUiConfig(distributionUIConfig);
        addGlobalUiConfig(globalUIConfig);
    }

}
