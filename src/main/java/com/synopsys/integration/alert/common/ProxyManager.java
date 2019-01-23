package com.synopsys.integration.alert.common;

import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class ProxyManager {
    private final BaseConfigurationAccessor configurationAccessor;

    @Autowired
    public ProxyManager(final BaseConfigurationAccessor configurationAccessor) {
        this.configurationAccessor = configurationAccessor;
    }

    private ConfigurationModel getSettingsConfiguration() throws AlertRuntimeException, AlertDatabaseConstraintException {
        return configurationAccessor.getConfigurationsByDescriptorName(SettingsDescriptor.SETTINGS_COMPONENT)
                   .stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertRuntimeException("Settings configuration missing"));
    }

    public Optional<String> getAlertProxyHost() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_HOST).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAlertProxyPort() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_PORT).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAlertProxyUsername() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_USERNAME).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAlertProxyPassword() {
        try {
            return getSettingsConfiguration().getField(SettingsDescriptor.KEY_PROXY_PASSWORD).flatMap(ConfigurationFieldModel::getFieldValue);
        } catch (AlertRuntimeException | AlertDatabaseConstraintException ex) {
            return Optional.empty();
        }
    }

    public ProxyInfo createProxyInfo() throws AlertRuntimeException, AlertDatabaseConstraintException, IllegalArgumentException {
        ConfigurationModel settingsConfiguration = getSettingsConfiguration();
        final Optional<String> alertProxyHost = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_HOST).flatMap(ConfigurationFieldModel::getFieldValue);
        final Optional<String> alertProxyPort = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_PORT).flatMap(ConfigurationFieldModel::getFieldValue);
        final Optional<String> alertProxyUsername = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_USERNAME).flatMap(ConfigurationFieldModel::getFieldValue);
        final Optional<String> alertProxyPassword = settingsConfiguration.getField(SettingsDescriptor.KEY_PROXY_PASSWORD).flatMap(ConfigurationFieldModel::getFieldValue);

        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        final CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        if (alertProxyUsername.isPresent()) {
            credentialsBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            credentialsBuilder.setPassword(alertProxyPassword.get());
        }
        proxyBuilder.setCredentials(credentialsBuilder.build());
        return proxyBuilder.build();
    }
}
