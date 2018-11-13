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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.connection.UnauthenticatedRestConnectionBuilder;

@Component
public class ChannelRestConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRestConnectionFactory.class);

    private final AlertProperties alertProperties;

    @Autowired
    public ChannelRestConnectionFactory(final AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public RestConnection createUnauthenticatedRestConnection(final String stringUrl) {
        final URL url = getUrlFromString(stringUrl);
        return createUnauthenticatedRestConnection(url);
    }

    public RestConnection createUnauthenticatedRestConnection(final URL url) {
        return createUnauthenticatedRestConnection(url, new Slf4jIntLogger(logger), 5 * 60 * 1000);
    }

    public RestConnection createUnauthenticatedRestConnection(final URL url, final IntLogger intLogger, final int timeout) {
        if (url == null) {
            logger.error("URL WAS NULL");
            return null;
        }
        final UnauthenticatedRestConnectionBuilder restConnectionBuilder = createUnauthenticatedRestConnectionBuilder(intLogger, url.toString(), timeout);

        final RestConnection connection = restConnectionBuilder.build();
        try {
            // the build operation will catch the issues based on the configuration settings and throw an exception
            // the IntegrationException caught here is unlikely to occur with an UnauthenticatedRestConnection.
            connection.connect();
            return connection;
        } catch (final IntegrationException e) {
            logger.error("Could not connect to " + url.toString(), e);
            return null;
        }
    }

    private URL getUrlFromString(final String apiUrl) {
        URL url = null;
        try {
            url = new URL(apiUrl);
        } catch (final MalformedURLException e) {
            logger.error("Problem generating the URL: " + apiUrl, e);
        }
        return url;
    }

    private UnauthenticatedRestConnectionBuilder createUnauthenticatedRestConnectionBuilder(final IntLogger logger, final String baseUrl, final int blackDuckTimeout) {
        final UnauthenticatedRestConnectionBuilder restConnectionBuilder = new UnauthenticatedRestConnectionBuilder();
        restConnectionBuilder.setLogger(logger);
        restConnectionBuilder.setTimeout(blackDuckTimeout);

        final Optional<String> alertProxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> alertProxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> alertProxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> alertProxyPassword = alertProperties.getAlertProxyPassword();
        final Optional<Boolean> alertTrustCertificate = alertProperties.getAlertTrustCertificate();
        restConnectionBuilder.setBaseUrl(baseUrl);
        if (alertProxyHost.isPresent()) {
            restConnectionBuilder.setProxyHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            restConnectionBuilder.setProxyPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent()) {
            restConnectionBuilder.setProxyUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            restConnectionBuilder.setProxyPassword(alertProxyPassword.get());
        }
        if (alertTrustCertificate.isPresent()) {
            restConnectionBuilder.setAlwaysTrustServerCertificate(alertTrustCertificate.get());
        }

        return restConnectionBuilder;
    }

}
