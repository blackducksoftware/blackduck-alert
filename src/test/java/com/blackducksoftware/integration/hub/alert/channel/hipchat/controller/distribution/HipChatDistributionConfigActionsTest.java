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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatManager;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionConfigActions;

public class HipChatDistributionConfigActionsTest {
    @Test
    public void testTestConfig() throws Exception {
        final HipChatManager hipChatManager = Mockito.mock(HipChatManager.class);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(null, null, null, null, null, hipChatManager);

        configActions.testConfig(null);
        verify(hipChatManager, times(1)).sendTestMessage(Mockito.any());
        Mockito.reset(hipChatManager);

        configActions.testConfig(null);
        verify(hipChatManager, times(1)).sendTestMessage(Mockito.any());
    }

}
