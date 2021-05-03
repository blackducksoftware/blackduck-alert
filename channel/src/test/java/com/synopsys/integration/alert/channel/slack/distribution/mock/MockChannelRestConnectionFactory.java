package com.synopsys.integration.alert.channel.slack.distribution.mock;

import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.util.ChannelRestConnectionFactory;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class MockChannelRestConnectionFactory extends ChannelRestConnectionFactory {
    public MockChannelRestConnectionFactory(AlertProperties alertProperties, ProxyManager proxyManager) {
        super(alertProperties, proxyManager);
    }

    @Override
    public IntHttpClient createIntHttpClient() {
        IntLogger intLogger = Mockito.mock(IntLogger.class);
        ProxyInfo proxyInfo = Mockito.mock(ProxyInfo.class);
        return new MockIntHttpClient(intLogger, 1, Boolean.TRUE, proxyInfo);
    }
}
