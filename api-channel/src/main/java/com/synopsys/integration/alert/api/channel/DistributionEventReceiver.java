/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.distribute.DistributionEvent;

public abstract class DistributionEventReceiver<D extends DistributionJobDetailsModel> extends AlertMessageListener<DistributionEvent> {
    protected DistributionEventReceiver(Gson gson, ChannelKey channelKey, DistributionEventHandler<D> distributionEventHandler) {
        super(gson, channelKey.getUniversalKey(), DistributionEvent.class, distributionEventHandler);
    }

}
