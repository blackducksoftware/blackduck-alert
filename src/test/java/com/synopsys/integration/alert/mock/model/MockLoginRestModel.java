package com.synopsys.integration.alert.mock.model;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.web.model.LoginConfig;

public class MockLoginRestModel extends MockRestModelUtil<LoginConfig> {
    private String blackDuckUsername = "blackDuckUsername";
    private String blackDuckPassword = "blackDuckPassword";
    private String id = "1L";

    public String getBlackDuckPassword() {
        return blackDuckPassword;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public void setBlackDuckUsername(final String blackDuckUsername) {
        this.blackDuckUsername = blackDuckUsername;
    }

    public void setBlackDuckPassword(final String blackDuckPassword) {
        this.blackDuckPassword = blackDuckPassword;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public LoginConfig createRestModel() {
        return new LoginConfig(blackDuckUsername, blackDuckPassword);
    }

    @Override
    public String getRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("blackDuckUsername", blackDuckUsername);
        json.addProperty("blackDuckPassword", blackDuckPassword);
        return json.toString();
    }

}
