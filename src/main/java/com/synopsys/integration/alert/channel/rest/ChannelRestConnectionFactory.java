/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.rest;

import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class ChannelRestConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRestConnectionFactory.class);

    private final AlertProperties alertProperties;

    @Autowired
    public ChannelRestConnectionFactory(final AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public RestConnection createRestConnection() {
        return createRestConnection(new Slf4jIntLogger(logger), 5 * 60 * 1000);
    }

    public RestConnection createRestConnection(final IntLogger intLogger, final int timeout) {
        final Optional<Boolean> alertTrustCertificate = alertProperties.getAlertTrustCertificate();
        final ProxyInfo proxyInfo = createProxyInfo();
        return new RestConnection(intLogger, timeout, alertTrustCertificate.orElse(Boolean.FALSE), proxyInfo);
    }

    private ProxyInfo createProxyInfo() {
        final Optional<String> alertProxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> alertProxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> alertProxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> alertProxyPassword = alertProperties.getAlertProxyPassword();

        final ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
        if (alertProxyHost.isPresent()) {
            proxyInfoBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyInfoBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent() && alertProxyPassword.isPresent()) {
            final CredentialsBuilder credentialBuilder = Credentials.newBuilder();
            credentialBuilder.setUsername(alertProxyUsername.get());
            credentialBuilder.setPassword(alertProxyPassword.get());
            proxyInfoBuilder.setCredentials(credentialBuilder.build());
        }
        return proxyInfoBuilder.build();
    }

}
