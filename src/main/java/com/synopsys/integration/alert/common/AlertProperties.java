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
package com.synopsys.integration.alert.common;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AlertProperties {
    @Value("${alert.config.home:}")
    private String alertConfigHome;

    @Value("${alert.templates.dir:}")
    private String alertTemplatesDir;

    @Value("${alert.images.dir:}")
    private String alertImagesDir;

    @Value("${alert.trust.cert:}")
    private Boolean alertTrustCertificate;

    @Value("${alert.proxy.host:}")
    private String alertProxyHost;

    @Value("${alert.proxy.port:}")
    private String alertProxyPort;

    @Value("${alert.proxy.username:}")
    private String alertProxyUsername;

    @Value("${alert.proxy.password:}")
    private String alertProxyPassword;

    @Value("${alert.encryption.password:}")
    private String alertEncryptionPassword;

    @Value("${alert.encryption.global.salt:}")
    private String alertEncryptionGlobalSalt;

    public String getAlertConfigHome() {
        return StringUtils.trimToNull(alertConfigHome);
    }

    public String getAlertTemplatesDir() {
        return StringUtils.trimToNull(alertTemplatesDir);
    }

    public String getAlertImagesDir() {
        return StringUtils.trimToNull(alertImagesDir);
    }

    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    public Optional<String> getAlertProxyHost() {
        return getOptionalString(alertProxyHost);
    }

    public Optional<String> getAlertProxyPort() {
        return getOptionalString(alertProxyPort);
    }

    public Optional<String> getAlertProxyUsername() {
        return getOptionalString(alertProxyUsername);
    }

    public Optional<String> getAlertProxyPassword() {
        return getOptionalString(alertProxyPassword);
    }

    public Optional<String> getAlertEncryptionPassword() {
        return getOptionalString(alertEncryptionPassword);
    }

    public Optional<String> getAlertEncryptionGlobalSalt() {
        return getOptionalString(alertEncryptionGlobalSalt);
    }

    private Optional<String> getOptionalString(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public void setAlertProxyHost(final String alertProxyHost) {
        this.alertProxyHost = alertProxyHost;
    }

    public void setAlertProxyPort(final String alertProxyPort) {
        this.alertProxyPort = alertProxyPort;
    }

    public void setAlertProxyUsername(final String alertProxyUsername) {
        this.alertProxyUsername = alertProxyUsername;
    }

    public void setAlertProxyPassword(final String alertProxyPassword) {
        this.alertProxyPassword = alertProxyPassword;
    }
}
