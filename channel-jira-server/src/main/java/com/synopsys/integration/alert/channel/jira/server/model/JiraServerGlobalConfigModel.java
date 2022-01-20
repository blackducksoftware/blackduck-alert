package com.synopsys.integration.alert.channel.jira.server.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;

public class JiraServerGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<JiraServerGlobalConfigModel> {
    private String url;
    private String userName;
    private Boolean isPasswordSet;
    private String password;
    private Boolean disablePluginCheck;

    @Override
    public JiraServerGlobalConfigModel obfuscate() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel();

        jiraServerGlobalConfigModel.setId(getId());
        jiraServerGlobalConfigModel.setName(getName());
        jiraServerGlobalConfigModel.setLastUpdated(getLastUpdated());
        jiraServerGlobalConfigModel.setCreatedAt(getCreatedAt());

        jiraServerGlobalConfigModel.setUrl(url);
        jiraServerGlobalConfigModel.setUserName(userName);
        jiraServerGlobalConfigModel.setDisablePluginCheck(disablePluginCheck);

        jiraServerGlobalConfigModel.setIsPasswordSet(StringUtils.isNotBlank(password));
        jiraServerGlobalConfigModel.setPassword(null);

        return jiraServerGlobalConfigModel;
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Optional<String> getUserName() {
        return Optional.ofNullable(userName);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getIsSmtpPasswordSet() {
        return isPasswordSet;
    }

    public void setIsPasswordSet(Boolean isPasswordSet) {
        this.isPasswordSet = isPasswordSet;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<Boolean> getDisablePluginCheck() {
        return Optional.ofNullable(disablePluginCheck);
    }

    public void setDisablePluginCheck(Boolean disablePluginCheck) {
        this.disablePluginCheck = disablePluginCheck;
    }
}
