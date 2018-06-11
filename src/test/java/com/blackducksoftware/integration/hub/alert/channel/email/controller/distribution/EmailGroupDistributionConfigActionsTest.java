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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;

public class EmailGroupDistributionConfigActionsTest {
    @Test
    public void testTestConfig() throws Exception {
        final DistributionChannelManager emailManager = Mockito.mock(DistributionChannelManager.class);
        final EmailGroupDistributionConfigActions configActions = new EmailGroupDistributionConfigActions(null, null, null, null, null, emailManager);

        configActions.testConfig(null);
        verify(emailManager, times(1)).sendTestMessage(Mockito.any(), Mockito.any());
        Mockito.reset(emailManager);

        configActions.testConfig(null);
        verify(emailManager, times(1)).sendTestMessage(Mockito.any(), Mockito.any());
    }

}
