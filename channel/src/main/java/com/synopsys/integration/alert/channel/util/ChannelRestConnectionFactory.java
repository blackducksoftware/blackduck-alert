/**
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
