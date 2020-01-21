/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.synopsys.integration.alert.common.exception.AlertException;

public class AlertProperties {
    @Value("${alert.config.home:}")
    private String alertConfigHome;

    @Deprecated
    @Value("${alert.templates.dir:}")
    private String alertTemplatesDir;

    @Value("${alert.images.dir:}")
    private String alertImagesDir;

    @Value("${alert.secrets.dir:/run/secrets}")
    private String alertSecretsDir;

    @Value("${alert.email.attachments.dir:./email/attachments}")
    private String alertEmailAttachmentsDir;

    @Value("${alert.trust.cert:}")
    private Boolean alertTrustCertificate;

    @Value("${alert.encryption.password:}")
    private String alertEncryptionPassword;

    @Value("${alert.encryption.global.salt:}")
    private String alertEncryptionGlobalSalt;

    @Value("${alert.logging.level:INFO}")
    private String loggingLevel;

    // SSL properties
    @Value("${server.port:}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.ssl.key-store:}")
    private String keyStoreFile;

    @Value("${server.ssl.key-store-password:}")
    private String keyStorePass;

    @Value("${server.ssl.keyStoreType:}")
    private String keyStoreType;

    @Value("${server.ssl.keyAlias:}")
    private String keyAlias;

    @Value("${server.ssl.trust-store:}")
    private String trustStoreFile;

    @Value("${server.ssl.trust-store-password:}")
    private String trustStorePass;

    @Value("${server.ssl.trustStoreType:}")
    private String trustStoreType;

    @Value("${spring.h2.console.enabled:false}")
    private Boolean h2ConsoleEnabled;

    @Value("${server.ssl.enabled:false}")
    private Boolean sslEnabled;

    // the blackduck product hasn't renamed their environment variables from hub to blackduck
    // need to keep hub in the name until
    @Value("${public.hub.webserver.host:}")
    private String publicWebserverHost;

    @Value("${public.hub.webserver.port:}")
    private String publicWebserverPort;

    @Value("${alert.hostname:}")
    private String alertHostName;

    public String getAlertConfigHome() {
        return StringUtils.trimToNull(alertConfigHome);
    }

    @Deprecated
    public String getAlertTemplatesDir() {
        return StringUtils.trimToNull(alertTemplatesDir);
    }

    public String getAlertImagesDir() {
        return StringUtils.trimToNull(alertImagesDir);
    }

    public String getAlertLogo() throws AlertException {
        String imagesDirectory = getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            return imagesDirectory + "/synopsys.png";
        }
        throw new AlertException(String.format("Could not find the Alert logo in the images directory '%s'", imagesDirectory));
    }

    public String getAlertSecretsDir() {
        return StringUtils.trimToNull(alertSecretsDir);
    }

    public String getAlertEmailAttachmentsDir() {
        return alertEmailAttachmentsDir;
    }

    public Boolean getH2ConsoleEnabled() {
        return h2ConsoleEnabled;
    }

    public Boolean getSslEnabled() {
        return sslEnabled;
    }

    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    public Optional<String> getAlertEncryptionPassword() {
        return getOptionalString(alertEncryptionPassword);
    }

    public Optional<String> getAlertEncryptionGlobalSalt() {
        return getOptionalString(alertEncryptionGlobalSalt);
    }

    public Optional<String> getLoggingLevel() {
        return getOptionalString(loggingLevel);
    }

    public Optional<String> getServerPort() {
        return getOptionalString(serverPort);
    }

    public Optional<String> getContextPath() {
        return getOptionalString(contextPath);
    }

    public Optional<String> getKeyAlias() {
        return getOptionalString(keyAlias);
    }

    public Optional<String> getKeyStoreFile() {
        return getOptionalString(keyStoreFile);
    }

    public Optional<String> getKeyStorePass() {
        return getOptionalString(keyStorePass);
    }

    public Optional<String> getKeyStoreType() {
        return getOptionalString(keyStoreType);
    }

    public String getTrustStoreFile() {
        return trustStoreFile;
    }

    public String getTrustStorePass() {
        return trustStorePass;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    @Deprecated
    public Optional<String> getPublicWebserverHost() {
        return getOptionalString(publicWebserverHost);
    }

    @Deprecated
    public Optional<String> getPublicWebserverPort() {
        return getOptionalString(publicWebserverPort);
    }

    public Optional<String> getAlertHostName() {
        return getOptionalString(alertHostName);
    }

    private Optional<String> getOptionalString(String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public Optional<String> getServerUrl() {
        try {
            String hostName = getAlertHostName().orElse(getPublicWebserverHost().orElse("localhost"));
            String port = getServerPort().orElse(getPublicWebserverPort().orElse(getServerPort().orElse("")));
            String path = getContextPath().orElse("");
            String protocol = "http";
            if (getSslEnabled()) {
                protocol = "https";
            }
            URL url;
            if (StringUtils.isNotBlank(port)) {
                url = new URL(protocol, hostName, Integer.parseInt(port), path);
            } else {
                url = new URL(protocol, hostName, path);
            }
            return Optional.of(url.toString());
        } catch (NumberFormatException | MalformedURLException ex) {
            return Optional.empty();
        }
    }
}
