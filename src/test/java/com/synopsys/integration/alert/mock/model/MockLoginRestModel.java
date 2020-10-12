package com.synopsys.integration.alert.mock.model;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.component.authentication.web.LoginConfig;

public class MockLoginRestModel extends MockRestModelUtil<LoginConfig> {
    private String alertUsername = "alertUsername";
    private String alertPassword = "alertPassword";
    private String id = "1L";

    public String getAlertPassword() {
        return alertPassword;
    }

    public String getAlertUsername() {
        return alertUsername;
    }

    public void setAlertUsername(String alertUsername) {
        this.alertUsername = alertUsername;
    }

    public void setAlertPassword(String alertPassword) {
        this.alertPassword = alertPassword;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public LoginConfig createRestModel() {
        return new LoginConfig(alertUsername, alertPassword);
    }

    @Override
    public String getRestModelJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("alertUsername", alertUsername);
        json.addProperty("alertPassword", alertPassword);
        return json.toString();
    }

}
