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
package com.synopsys.integration.alert.provider.blackduck.mock;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.mock.MockGlobalRestModelUtil;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckConfig;

public class MockGlobalBlackDuckRestModel extends MockGlobalRestModelUtil<BlackDuckConfig> {
    private String blackDuckUrl;
    private String blackDuckTimeout;
    private String blackDuckUsername;
    private String blackDuckPassword;
    private String blackDuckApiKey;
    private boolean blackDuckApiKeyIsSet;
    private String blackDuckProxyHost;
    private String blackDuckProxyPort;
    private String blackDuckProxyUsername;
    private String blackDuckProxyPassword;
    private boolean blackDuckProxyPasswordIsSet;
    private String blackDuckAlwaysTrustCertificate;
    private String id;

    public MockGlobalBlackDuckRestModel() {
        this("BlackDuckUrl", "444", "BlackDuckUsername", "BlackDuckPassword", "BlackDuckApiKey############################################################", false, "BlackDuckProxyHost", "555", "BlackDuckProxyUsername",
                "BlackDuckProxyPassword", true, "true", "1");
    }

    private MockGlobalBlackDuckRestModel(final String blackDuckUrl, final String blackDuckTimeout, final String blackDuckUsername, final String blackDuckPassword, final String blackDuckApiKey, final boolean blackDuckApiKeyIsSet,
            final String blackDuckProxyHost,
            final String blackDuckProxyPort,
            final String blackDuckProxyUsername, final String blackDuckProxyPassword, final boolean blackDuckProxyPasswordIsSet, final String blackDuckAlwaysTrustCertificate, final String id) {
        this.blackDuckUrl = blackDuckUrl;
        this.blackDuckTimeout = blackDuckTimeout;
        this.blackDuckUsername = blackDuckUsername;
        this.blackDuckPassword = blackDuckPassword;
        this.blackDuckApiKey = blackDuckApiKey;
        this.blackDuckApiKeyIsSet = blackDuckApiKeyIsSet;
        this.blackDuckProxyHost = blackDuckProxyHost;
        this.blackDuckProxyPort = blackDuckProxyPort;
        this.blackDuckProxyUsername = blackDuckProxyUsername;
        this.blackDuckProxyPassword = blackDuckProxyPassword;
        this.blackDuckProxyPasswordIsSet = blackDuckProxyPasswordIsSet;
        this.blackDuckAlwaysTrustCertificate = blackDuckAlwaysTrustCertificate;
        this.id = id;
    }

    public String getHubUrl() {
        return blackDuckUrl;
    }

    public String getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public String getBlackDuckApiKey() {
        return blackDuckApiKey;
    }

    public String getBlackDuckProxyHost() {
        return blackDuckProxyHost;
    }

    public String getBlackDuckProxyPort() {
        return blackDuckProxyPort;
    }

    public String getBlackDuckProxyUsername() {
        return blackDuckProxyUsername;
    }

    public String getBlackDuckProxyPassword() {
        return blackDuckProxyPassword;
    }

    public String getBlackDuckAlwaysTrustCertificate() {
        return blackDuckAlwaysTrustCertificate;
    }

    public boolean isBlackDuckApiKeyIsSet() {
        return blackDuckApiKeyIsSet;
    }

    public void setBlackDuckApiKeyIsSet(final boolean blackDuckApiKeyIsSet) {
        this.blackDuckApiKeyIsSet = blackDuckApiKeyIsSet;
    }

    public boolean isBlackDuckProxyPasswordIsSet() {
        return blackDuckProxyPasswordIsSet;
    }

    public void setBlackDuckProxyPasswordIsSet(final boolean blackDuckProxyPasswordIsSet) {
        this.blackDuckProxyPasswordIsSet = blackDuckProxyPasswordIsSet;
    }

    public void setBlackDuckUrl(final String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
    }

    public void setBlackDuckTimeout(final String blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    public void setBlackDuckUsername(final String blackDuckUsername) {
        this.blackDuckUsername = blackDuckUsername;
    }

    public void setBlackDuckPassword(final String blackDuckPassword) {
        this.blackDuckPassword = blackDuckPassword;
    }

    public void setBlackDuckApiKey(final String blackDuckApiKey) {
        this.blackDuckApiKey = blackDuckApiKey;
    }

    public void setBlackDuckProxyHost(final String blackDuckProxyHost) {
        this.blackDuckProxyHost = blackDuckProxyHost;
    }

    public void setBlackDuckProxyPort(final String blackDuckProxyPort) {
        this.blackDuckProxyPort = blackDuckProxyPort;
    }

    public void setBlackDuckProxyUsername(final String blackDuckProxyUsername) {
        this.blackDuckProxyUsername = blackDuckProxyUsername;
    }

    public void setBlackDuckProxyPassword(final String blackDuckProxyPassword) {
        this.blackDuckProxyPassword = blackDuckProxyPassword;
    }

    public void setBlackDuckAlwaysTrustCertificate(final String blackDuckAlwaysTrustCertificate) {
        this.blackDuckAlwaysTrustCertificate = blackDuckAlwaysTrustCertificate;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public BlackDuckConfig createGlobalRestModel() {
        final BlackDuckConfig restModel = new BlackDuckConfig(id, blackDuckUrl, blackDuckTimeout, blackDuckApiKey, blackDuckApiKeyIsSet, blackDuckProxyHost, blackDuckProxyPort, blackDuckProxyUsername, blackDuckProxyPassword,
                blackDuckProxyPasswordIsSet,
                blackDuckAlwaysTrustCertificate);
        return restModel;
    }

    @Override
    public BlackDuckConfig createEmptyGlobalRestModel() {
        return new BlackDuckConfig();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("blackDuckUrl", blackDuckUrl);
        json.addProperty("blackDuckTimeout", blackDuckTimeout);
        json.addProperty("blackDuckApiKey", blackDuckApiKey);
        json.addProperty("blackDuckApiKeyIsSet", blackDuckApiKeyIsSet);
        json.addProperty("blackDuckProxyHost", blackDuckProxyHost);
        json.addProperty("blackDuckProxyPort", blackDuckProxyPort);
        json.addProperty("blackDuckProxyUsername", blackDuckProxyUsername);
        json.addProperty("blackDuckProxyPassword", blackDuckProxyPassword);
        json.addProperty("blackDuckProxyPasswordIsSet", blackDuckProxyPasswordIsSet);
        json.addProperty("blackDuckAlwaysTrustCertificate", blackDuckAlwaysTrustCertificate);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("blackDuckApiKeyIsSet", false);
        json.addProperty("blackDuckProxyPasswordIsSet", false);
        return json.toString();
    }

}
