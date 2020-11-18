/**
 * test-common
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
package com.synopsys.integration.alert.test.common;

import java.util.Optional;

import com.synopsys.integration.alert.common.AlertProperties;

// TODO rename class to include "Mock"
public class TestAlertProperties extends AlertProperties {
    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String RESOURCES_PATH = "/src/main/resources";
    public static final String IMAGES_PATH = RESOURCES_PATH + "/images/";

    private String alertConfigHome;
    private String alertImagesDir;
    private String alertSecretsDir;
    private Boolean alertTrustCertificate;
    private String alertProxyHost;
    private String alertProxyPort;
    private String alertProxyUsername;
    private String alertProxyPassword;
    private boolean sslEnabled = false;
    private String encryptionPassword;
    private String encryptionSalt;

    public TestAlertProperties() {
        String userDirectory = System.getProperties().getProperty(PROPERTY_USER_DIR);
        alertImagesDir = userDirectory + IMAGES_PATH;

        encryptionPassword = "changeme";
        encryptionSalt = "changeme";
        this.alertSecretsDir = "./testDB/run/secrets";
    }

    @Override
    public String getAlertConfigHome() {
        return alertConfigHome;
    }

    public void setAlertConfigHome(String alertConfigHome) {
        this.alertConfigHome = alertConfigHome;
    }

    @Override
    public String getAlertImagesDir() {
        return alertImagesDir;
    }

    public void setAlertImagesDir(String alertImagesDir) {
        this.alertImagesDir = alertImagesDir;
    }

    @Override
    public Optional<Boolean> getAlertTrustCertificate() {
        return Optional.ofNullable(alertTrustCertificate);
    }

    public void setAlertTrustCertificate(Boolean alertTrustCertificate) {
        this.alertTrustCertificate = alertTrustCertificate;
    }

    @Override
    public boolean getSslEnabled() {
        return this.sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    @Override
    public Optional<String> getAlertEncryptionGlobalSalt() {
        return Optional.of(encryptionSalt);
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    @Override
    public Optional<String> getAlertEncryptionPassword() {
        return Optional.of(encryptionPassword);
    }

    public void setEncryptionSalt(String encryptionSalt) {
        this.encryptionSalt = encryptionSalt;
    }

    @Override
    public String getAlertSecretsDir() {
        return this.alertSecretsDir;
    }

    public void setAlertSecretsDir(String alertSecretsDir) {
        this.alertSecretsDir = alertSecretsDir;
    }

}
