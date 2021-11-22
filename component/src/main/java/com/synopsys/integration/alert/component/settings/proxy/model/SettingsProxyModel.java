package com.synopsys.integration.alert.component.settings.proxy.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;

public class SettingsProxyModel extends ConfigWithMetadata implements Obfuscated<SettingsProxyModel> {
    @JsonProperty("proxyHost")
    private String host;
    @JsonProperty("proxyPort")
    private Integer port;
    @JsonProperty("proxyUsername")
    private String username;
    @JsonProperty("proxyPassword")
    private String password;
    @JsonProperty("nonProxyHosts")
    private String nonProxyHosts;

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<String> getNonProxyHosts() {
        return Optional.ofNullable(nonProxyHosts);
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    @Override
    public SettingsProxyModel obfuscate() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();

        settingsProxyModel.setId(getId());
        settingsProxyModel.setLastUpdated(getLastUpdated());
        settingsProxyModel.setCreatedAt(getCreatedAt());

        settingsProxyModel.setHost(host);
        settingsProxyModel.setPort(port);
        settingsProxyModel.setUsername(username);
        settingsProxyModel.setNonProxyHosts(nonProxyHosts);

        String maskedPassword = (password != null) ? ConfigurationCrudHelper.MASKED_VALUE : null;
        settingsProxyModel.setPassword(maskedPassword);

        return settingsProxyModel;
    }
}
