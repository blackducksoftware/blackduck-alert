/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.hub.mock;

import com.blackducksoftware.integration.hub.alert.hub.controller.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalRestModelUtil;
import com.google.gson.JsonObject;

public class MockGlobalHubRestModel extends MockGlobalRestModelUtil<GlobalHubConfigRestModel> {
    private String hubUrl;
    private String hubTimeout;
    private String hubUsername;
    private String hubPassword;
    private String hubApiKey;
    private boolean hubApiKeyIsSet;
    private String hubProxyHost;
    private String hubProxyPort;
    private String hubProxyUsername;
    private String hubProxyPassword;
    private boolean hubProxyPasswordIsSet;
    private String hubAlwaysTrustCertificate;
    private String id;

    public MockGlobalHubRestModel() {
        this("HubUrl", "444", "HubUsername", "HubPassword", "HubApiKey############################################################", false, "HubProxyHost", "555", "HubProxyUsername", "HubProxyPassword", true, "true", "1");
    }

    private MockGlobalHubRestModel(final String hubUrl, final String hubTimeout, final String hubUsername, final String hubPassword, final String hubApiKey, final boolean hubApiKeyIsSet, final String hubProxyHost, final String hubProxyPort,
            final String hubProxyUsername, final String hubProxyPassword, final boolean hubProxyPasswordIsSet, final String hubAlwaysTrustCertificate, final String id) {
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.hubApiKey = hubApiKey;
        this.hubApiKeyIsSet = hubApiKeyIsSet;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubProxyPasswordIsSet = hubProxyPasswordIsSet;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
        this.id = id;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getHubTimeout() {
        return hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public String getHubApiKey() {
        return hubApiKey;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public String getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    public boolean isHubApiKeyIsSet() {
        return hubApiKeyIsSet;
    }

    public void setHubApiKeyIsSet(final boolean hubApiKeyIsSet) {
        this.hubApiKeyIsSet = hubApiKeyIsSet;
    }

    public boolean isHubProxyPasswordIsSet() {
        return hubProxyPasswordIsSet;
    }

    public void setHubProxyPasswordIsSet(final boolean hubProxyPasswordIsSet) {
        this.hubProxyPasswordIsSet = hubProxyPasswordIsSet;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public void setHubTimeout(final String hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public void setHubUsername(final String hubUsername) {
        this.hubUsername = hubUsername;
    }

    public void setHubPassword(final String hubPassword) {
        this.hubPassword = hubPassword;
    }

    public void setHubApiKey(final String hubApiKey) {
        this.hubApiKey = hubApiKey;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public void setHubAlwaysTrustCertificate(final String hubAlwaysTrustCertificate) {
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalHubConfigRestModel createGlobalRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubApiKey, hubApiKeyIsSet, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubProxyPasswordIsSet,
                hubAlwaysTrustCertificate);
        return restModel;
    }

    @Override
    public GlobalHubConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalHubConfigRestModel();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubUrl", hubUrl);
        json.addProperty("hubTimeout", hubTimeout);
        json.addProperty("hubApiKey", hubApiKey);
        json.addProperty("hubApiKeyIsSet", hubApiKeyIsSet);
        json.addProperty("hubProxyHost", hubProxyHost);
        json.addProperty("hubProxyPort", hubProxyPort);
        json.addProperty("hubProxyUsername", hubProxyUsername);
        json.addProperty("hubProxyPassword", hubProxyPassword);
        json.addProperty("hubProxyPasswordIsSet", hubProxyPasswordIsSet);
        json.addProperty("hubAlwaysTrustCertificate", hubAlwaysTrustCertificate);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubApiKeyIsSet", false);
        json.addProperty("hubProxyPasswordIsSet", false);
        return json.toString();
    }

}
