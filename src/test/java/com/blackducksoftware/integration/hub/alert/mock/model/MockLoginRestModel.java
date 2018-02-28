package com.blackducksoftware.integration.hub.alert.mock.model;

import com.blackducksoftware.integration.hub.alert.web.model.LoginRestModel;
import com.google.gson.JsonObject;

public class MockLoginRestModel extends MockRestModelUtil<LoginRestModel> {
    private String hubUsername;
    private String hubPassword;
    private String id;

    public MockLoginRestModel() {
        this("hubUsername", "hubPassword");
    }

    private MockLoginRestModel(final String hubUsername, final String hubPassword) {
        super();
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.id = "1L";
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public void setHubUsername(final String hubUsername) {
        this.hubUsername = hubUsername;
    }

    public void setHubPassword(final String hubPassword) {
        this.hubPassword = hubPassword;
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
        return new LoginRestModel(hubUsername, hubPassword);
    }

    @Override
    public LoginRestModel createEmptyRestModel() {
        return new LoginRestModel();
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("hubUsername", hubUsername);
        return json.toString();
    }

}
