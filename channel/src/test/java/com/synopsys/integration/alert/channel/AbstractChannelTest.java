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

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.rest.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.channel.api.rest.RestChannelUtility;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public abstract class AbstractChannelTest {
    protected Gson gson;
    protected TestProperties properties;

    @BeforeEach
    public void init() {
        gson = new Gson();
        properties = new TestProperties();
    }

    public RestChannelUtility createRestChannelUtility() {
        MockAlertProperties testAlertProperties = new MockAlertProperties();
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfo()).thenReturn(ProxyInfo.NO_PROXY_INFO);
        ChannelRestConnectionFactory channelRestConnectionFactory = new ChannelRestConnectionFactory(testAlertProperties, proxyManager);
        return new RestChannelUtility(channelRestConnectionFactory);
    }

}
