/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.util.ExcludedIncludedWildcardFilter;

@Component
public class ProxyManager {
    public static final String KEY_PROXY_HOST = "settings.proxy.host";
    public static final String KEY_PROXY_PORT = "settings.proxy.port";
    public static final String KEY_PROXY_USERNAME = "settings.proxy.username";
    public static final String KEY_PROXY_PWD = "settings.proxy.password";
    public static final String KEY_PROXY_NON_PROXY_HOSTS = "settings.proxy.non.proxy.hosts";

    private final Logger logger = LoggerFactory.getLogger(ProxyManager.class);

    private final SettingsUtility settingsUtility;

    @Autowired
    public ProxyManager(SettingsUtility settingsUtility) {
        this.settingsUtility = settingsUtility;
    }

    public ProxyInfo createProxyInfo() throws IllegalArgumentException {
        Optional<ConfigurationModel> optionalSettingsConfiguration = settingsUtility.getConfiguration();
        if (optionalSettingsConfiguration.isPresent()) {
            return createProxyInfo(optionalSettingsConfiguration.get());
        }
        return ProxyInfo.NO_PROXY_INFO;
    }

    public ProxyInfo createProxyInfoForHost(String proxyHostCandidate) throws IllegalArgumentException {
        Optional<ConfigurationModel> optionalSettingsConfiguration = settingsUtility.getConfiguration();
        if (optionalSettingsConfiguration.isPresent()) {
            ConfigurationModel settingsConfiguration = optionalSettingsConfiguration.get();

            Collection<String> nonProxyHosts = extractNonProxyHosts(settingsConfiguration);
            NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(nonProxyHosts);
            if (StringUtils.isNotBlank(proxyHostCandidate) && nonProxyHostChecker.isNonProxyHost(proxyHostCandidate)) {
                return ProxyInfo.NO_PROXY_INFO;
            }

            return createProxyInfo(settingsConfiguration);
        }
        return ProxyInfo.NO_PROXY_INFO;
    }

    private Optional<String> extractProxySetting(ConfigurationModel settingsConfiguration, String key) {
        return settingsConfiguration.getField(key).flatMap(ConfigurationFieldModel::getFieldValue);
    }

    private ProxyInfo createProxyInfo(ConfigurationModel settingsConfiguration) {
        Optional<String> alertProxyHost = extractProxySetting(settingsConfiguration, KEY_PROXY_HOST);
        Optional<String> alertProxyPort = extractProxySetting(settingsConfiguration, KEY_PROXY_PORT);
        Optional<String> alertProxyUsername = extractProxySetting(settingsConfiguration, KEY_PROXY_USERNAME);
        Optional<String> alertProxyPassword = extractProxySetting(settingsConfiguration, KEY_PROXY_PWD);

        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        alertProxyHost.ifPresent(proxyBuilder::setHost);
        alertProxyPort.map(NumberUtils::toInt).ifPresent(proxyBuilder::setPort);

        CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        alertProxyUsername.ifPresent(credentialsBuilder::setUsername);
        alertProxyPassword.ifPresent(credentialsBuilder::setPassword);
        proxyBuilder.setCredentials(credentialsBuilder.build());

        return proxyBuilder.build();
    }

    private Collection<String> extractNonProxyHosts(ConfigurationModel settingsConfiguration) {
        return settingsConfiguration
                   .getField(KEY_PROXY_NON_PROXY_HOSTS)
                   .map(ConfigurationFieldModel::getFieldValues)
                   .orElseGet(Set::of);
    }

    public static class NonProxyHostChecker {
        private final Collection<String> nonProxyHosts;
        private final ExcludedIncludedWildcardFilter wildcardFilter;

        public NonProxyHostChecker(Collection<String> nonProxyHosts) {
            this.nonProxyHosts = nonProxyHosts;
            this.wildcardFilter = ExcludedIncludedWildcardFilter.fromCollections(Set.of(), nonProxyHosts);
        }

        public boolean isNonProxyHost(String proxyHostCandidate) {
            if (nonProxyHosts.isEmpty()) {
                return false;
            }

            if (includesOrContains(proxyHostCandidate)) {
                return true;
            }

            try {
                URL url = new URL(proxyHostCandidate);
                String urlHost = url.getHost();
                return includesOrContains(urlHost);
            } catch (MalformedURLException e) {
                return false;
            }
        }

        private boolean includesOrContains(String candidate) {
            return wildcardFilter.willInclude(candidate) || nonProxyHosts.contains(candidate);
        }

    }

}
