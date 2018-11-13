/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class DistributionChannelManagerTestIT<G extends GlobalChannelConfigEntity, D extends DistributionChannelConfigEntity, R extends CommonDistributionConfig> extends AlertIntegrationTest {

    @SuppressWarnings("rawtypes")
    @Autowired
    private List<DistributionChannel> channelList;
    @Autowired
    private List<ChannelDescriptor> channelDescriptorList;

    @Test
    public void assertManagerHasBeenCreatedForChannel() {
        // DO NOT DELETE THIS TEST
        // This test exists to ensure that when a new channel is created, its necessary pieces are created as well.
        assertEquals(channelList.size(), channelDescriptorList.size());
    }

}
