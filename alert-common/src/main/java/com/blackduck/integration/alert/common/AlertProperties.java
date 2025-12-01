/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common;

import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public class AlertProperties {
    public static final String FILE_NAME_BLACKDUCK_LOGO = "BlackDuckLogo.png";

    @Value("${alert.config.home:}")
    private String alertConfigHome;

    @Value("${alert.images.dir:}")
    private String alertImagesDir;

    @Value("${alert.secrets.dir:/run/secrets}")
    private String alertSecretsDir;

    @Value("${alert.email.attachments.dir:/tmp}")
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

    @Value("${public.alert.webserver.port:}")
    private String publicServerPort;

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

    @Value("${server.ssl.enabled:false}")
    private Boolean sslEnabled;

    @Value("${alert.hostname:}")
    private String alertHostName;

    @Value("${alert.login.lockout.threshold:10}")
    private Long loginLockoutThreshold;
    @Value("${alert.login.lockout.minutes:10}")
    private Long loginLockoutMinutes;

    @Value("${alert.notification.mapping.batch.limit:10000}")
    private Integer notificationMappingBatchLimit;

    @Value("${alert.notification.accumulator.batch.limit:10000}")
    private Integer accumulatorNotificationBatchLimit;

    public String getAlertConfigHome() {
        return StringUtils.trimToNull(alertConfigHome);
    }

    public String getAlertImagesDir() {
        return StringUtils.trimToNull(alertImagesDir);
    }

    public String createBlackDuckLogoPath() throws AlertException {
        String imagesDirectory = getAlertImagesDir();
        if (StringUtils.isNotBlank(imagesDirectory)) {
            Path blackduckLogoPath = Path.of(imagesDirectory, FILE_NAME_BLACKDUCK_LOGO);
            return blackduckLogoPath.toString();
        }
        throw new AlertException(String.format("Could not find the Alert logo in the images directory '%s'", imagesDirectory));
    }

    public String getAlertSecretsDir() {
        return StringUtils.trimToNull(alertSecretsDir);
    }

    public String getAlertEmailAttachmentsDir() {
        return alertEmailAttachmentsDir;
    }

    public boolean getSslEnabled() {
        return BooleanUtils.isTrue(sslEnabled);
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

    public Optional<String> getPublicServerPort() {
        return getOptionalString(publicServerPort);
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

    public Optional<String> getTrustStoreFile() {
        return getOptionalString(trustStoreFile);
    }

    public Optional<String> getTrustStorePass() {
        return getOptionalString(trustStorePass);
    }

    public Optional<String> getTrustStoreType() {
        return getOptionalString(trustStoreType);
    }

    public Optional<String> getAlertHostName() {
        return getOptionalString(alertHostName);
    }

    public Long getLoginLockoutThreshold() {
        return loginLockoutThreshold;
    }

    public Long getLoginLockoutMinutes() {
        return loginLockoutMinutes;
    }

    public Optional<Integer> getNotificationMappingBatchLimit() {
        return Optional.ofNullable(notificationMappingBatchLimit);
    }
    public Optional<Integer> getAccumulatorNotificationBatchLimit() {return Optional.ofNullable(accumulatorNotificationBatchLimit);}

    private Optional<String> getOptionalString(String value) {
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public String getServerURL() {
        return createPopulatedUriComponentsBuilderForServerURL().toUriString();
    }

    public UriComponentsBuilder createPopulatedUriComponentsBuilderForServerURL() {
        String scheme = getSslEnabled() ? "https" : "http";
        String hostName = getAlertHostName().orElse("localhost");
        String port = getPublicServerPort().or(this::getServerPort).orElse("8443");
        String path = getContextPath().orElse("alert");

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
        uriComponentsBuilder.scheme(scheme);
        uriComponentsBuilder.host(hostName);
        uriComponentsBuilder.port(port);
        uriComponentsBuilder.path(path);
        uriComponentsBuilder.path("/");
        return uriComponentsBuilder;
    }

}
