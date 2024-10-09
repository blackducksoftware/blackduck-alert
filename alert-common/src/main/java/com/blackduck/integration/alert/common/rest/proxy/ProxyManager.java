/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.descriptor.accessor.SettingsUtility;
import com.blackduck.integration.rest.credentials.CredentialsBuilder;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.proxy.ProxyInfoBuilder;
import com.blackduck.integration.util.ExcludedIncludedWildcardFilter;

@Component
public class ProxyManager {
    public static final String KEY_PROXY_HOST = "settings.proxy.host";
    public static final String KEY_PROXY_PORT = "settings.proxy.port";
    public static final String KEY_PROXY_USERNAME = "settings.proxy.username";
    public static final String KEY_PROXY_PWD = "settings.proxy.password";
    public static final String KEY_PROXY_NON_PROXY_HOSTS = "settings.proxy.non.proxy.hosts";

    private final SettingsUtility settingsUtility;

    @Autowired
    public ProxyManager(SettingsUtility settingsUtility) {
        this.settingsUtility = settingsUtility;
    }

    public ProxyInfo createProxyInfo() throws IllegalArgumentException {
        Optional<SettingsProxyModel> optionalSettingsConfiguration = settingsUtility.getConfiguration();
        if (optionalSettingsConfiguration.isPresent()) {
            return createProxyInfo(optionalSettingsConfiguration.get());
        }
        return ProxyInfo.NO_PROXY_INFO;
    }

    public ProxyInfo createProxyInfoForHost(String proxyHostCandidate) throws IllegalArgumentException {
        Optional<SettingsProxyModel> optionalSettingsConfiguration = settingsUtility.getConfiguration();
        if (optionalSettingsConfiguration.isPresent()) {
            SettingsProxyModel settingsProxyModel = optionalSettingsConfiguration.get();

            List<String> nonProxyHosts = settingsProxyModel.getNonProxyHosts().orElse(List.of());
            NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(nonProxyHosts);
            if (StringUtils.isNotBlank(proxyHostCandidate) && nonProxyHostChecker.isNonProxyHost(proxyHostCandidate)) {
                return ProxyInfo.NO_PROXY_INFO;
            }
            return createProxyInfo(settingsProxyModel);
        }
        return ProxyInfo.NO_PROXY_INFO;
    }

    public ProxyInfo createProxyInfo(SettingsProxyModel settingsProxyModel) {
        ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        proxyBuilder.setHost(settingsProxyModel.getProxyHost());
        proxyBuilder.setPort(settingsProxyModel.getProxyPort());

        CredentialsBuilder credentialsBuilder = new CredentialsBuilder();
        settingsProxyModel.getProxyUsername().ifPresent(credentialsBuilder::setUsername);
        settingsProxyModel.getProxyPassword().ifPresent(credentialsBuilder::setPassword);
        proxyBuilder.setCredentials(credentialsBuilder.build());

        return proxyBuilder.build();
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
