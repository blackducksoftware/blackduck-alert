package com.blackducksoftware.integration.hub.alert.mock.model;

import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.google.gson.JsonObject;

public class MockLoginRestModel extends MockRestModelUtil<LoginRestModel> {
    private String hubUrl;
    private String hubTimeout;
    private String hubUsername;
    private String hubPassword;
    private String hubProxyHost;
    private String hubProxyPort;
    private String hubProxyUsername;
    private String hubProxyPassword;
    private String hubAlwaysTrustCertificate;
    private String id;

    public MockLoginRestModel() {
        this("hubUrl", "400", "hubUsername", "hubPassword", "hubProxyHost", "500", "hubProxyUsername", "hubProxyPassword", "true", "1L");
    }

    private MockLoginRestModel(final String hubUrl, final String hubTimeout, final String hubUsername, final String hubPassword, final String hubProxyHost, final String hubProxyPort, final String hubProxyUsername,
            final String hubProxyPassword, final String hubAlwaysTrustCertificate, final String id) {
        super();
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
        this.id = id;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final String hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public void setHubUsername(final String hubUsername) {
        this.hubUsername = hubUsername;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public String getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    public void setHubAlwaysTrustCertificate(final String hubAlwaysTrustCertificate) {
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
    }

    public void setHubPassword(final String hubPassword) {
        this.hubPassword = hubPassword;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public LoginRestModel createRestModel() {
        return new LoginRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate);
    }

    @Override
    public LoginRestModel createEmptyRestModel() {
        return new LoginRestModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("hubUrl", hubUrl);
        json.addProperty("hubTimeout", hubTimeout);
        json.addProperty("hubUsername", hubUsername);
        json.addProperty("hubProxyHost", hubProxyHost);
        json.addProperty("hubProxyPort", hubProxyPort);
        json.addProperty("hubProxyUsername", hubProxyUsername);
        json.addProperty("hubAlwaysTrustCertificate", hubAlwaysTrustCertificate);
        return json.toString();
    }

}
