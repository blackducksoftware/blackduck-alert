/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.rest;

import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.certificates.AlertSSLContextManager;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;

@Component
public class ChannelRestConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(ChannelRestConnectionFactory.class);

    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;
    private final Gson gson;

    private final AlertSSLContextManager alertSSLContextManager;

    @Autowired
    public ChannelRestConnectionFactory(AlertProperties alertProperties, ProxyManager proxyManager, Gson gson, AlertSSLContextManager alertSSLContextManager) {
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
        this.gson = gson;
        this.alertSSLContextManager = alertSSLContextManager;
    }

    public IntHttpClient createIntHttpClient(String baseUrl) {
        return createIntHttpClient(baseUrl, new Slf4jIntLogger(logger), 5 * 60 * 1000);
    }

    public IntHttpClient createIntHttpClient(String baseUrl, IntLogger intLogger, int timeout) {
        Optional<Boolean> alertTrustCertificate = alertProperties.getAlertTrustCertificate();
        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(baseUrl);
        Optional<SSLContext> sslContext = alertSSLContextManager.buildWithClientCertificate();
        return sslContext
            .map(context -> new IntHttpClient(intLogger, gson, timeout, proxyInfo, context))
            .orElseGet(() -> new IntHttpClient(intLogger, gson, timeout, alertTrustCertificate.orElse(Boolean.FALSE), proxyInfo));
    }

}
