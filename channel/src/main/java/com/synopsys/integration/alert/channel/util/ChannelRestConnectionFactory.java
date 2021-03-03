/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class ChannelRestConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(ChannelRestConnectionFactory.class);

    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;

    @Autowired
    public ChannelRestConnectionFactory(AlertProperties alertProperties, ProxyManager proxyManager) {
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
    }

    public IntHttpClient createIntHttpClient() {
        return createIntHttpClient(new Slf4jIntLogger(logger), 5 * 60 * 1000);
    }

    public IntHttpClient createIntHttpClient(IntLogger intLogger, int timeout) {
        Optional<Boolean> alertTrustCertificate = alertProperties.getAlertTrustCertificate();
        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        return new IntHttpClient(intLogger, timeout, alertTrustCertificate.orElse(Boolean.FALSE), proxyInfo);
    }
}
