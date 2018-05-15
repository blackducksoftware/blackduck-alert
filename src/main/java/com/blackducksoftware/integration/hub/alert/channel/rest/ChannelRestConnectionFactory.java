/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.rest;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnectionBuilder;

@Component
public class ChannelRestConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChannelRestConnectionFactory.class);

    private final GlobalProperties globalProperties;

    @Autowired
    public ChannelRestConnectionFactory(final GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    public RestConnection createUnauthenticatedRestConnection(final String stringUrl) {
        final URL url = getUrlFromString(stringUrl);
        return createUnauthenticatedRestConnection(url);
    }

    public RestConnection createUnauthenticatedRestConnection(final URL url) {
        int timeout = 5 * 60;
        if (globalProperties.getHubTimeout() != null) {
            timeout = globalProperties.getHubTimeout();
        }

        return createUnauthenticatedRestConnection(url, new Slf4jIntLogger(logger), timeout * 1000);
    }

    public RestConnection createUnauthenticatedRestConnection(final URL url, final IntLogger intLogger, final int timeout) {
        if (url == null) {
            logger.error("URL WAS NULL");
            return null;
        }
        final UnauthenticatedRestConnectionBuilder restConnectionBuilder = new UnauthenticatedRestConnectionBuilder();
        restConnectionBuilder.setBaseUrl(url.toString());
        restConnectionBuilder.setLogger(intLogger);
        if (globalProperties.getHubTrustCertificate() != null) {
            restConnectionBuilder.setAlwaysTrustServerCertificate(globalProperties.getHubTrustCertificate());
        }
        restConnectionBuilder.setProxyHost(globalProperties.getHubProxyHost());
        if (globalProperties.getHubProxyPort() != null) {
            restConnectionBuilder.setProxyPort(NumberUtils.toInt(globalProperties.getHubProxyPort()));
        }
        if (globalProperties.getHubProxyUsername() != null) {
            restConnectionBuilder.setProxyUsername(globalProperties.getHubProxyUsername());
        }
        if (globalProperties.getHubProxyPassword() != null) {
            restConnectionBuilder.setProxyPassword(globalProperties.getHubProxyPassword());
        }
        restConnectionBuilder.setTimeout(timeout);

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

}
