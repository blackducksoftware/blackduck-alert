package com.synopsys.integration.alert.web.model;

import com.synopsys.integration.alert.common.annotation.SensitiveField;

public class SystemSetupModel extends MaskedModel {
    private final String blackDuckProviderUrl;
    private final Integer blackDuckConnectionTimeout;
    @SensitiveField
    private final String blackDuckApiToken;
    @SensitiveField
    private final String globalEncryptionPassword;
    @SensitiveField
    private final String globalEncryptionSalt;

    private final String proxyHost;
    private final String proxyPort;
    private final String proxyUsername;
    @SensitiveField
    private final String proxyPassword;

    public SystemSetupModel(final String blackDuckProviderUrl, final Integer blackDuckConnectionTimeout, final String blackDuckApiToken, final String globalEncryptionPassword, final String globalEncryptionSalt,
        final String proxyHost, final String proxyPort, final String proxyUsername, final String proxyPassword) {
        this.blackDuckProviderUrl = blackDuckProviderUrl;
        this.blackDuckConnectionTimeout = blackDuckConnectionTimeout;
        this.blackDuckApiToken = blackDuckApiToken;
        this.globalEncryptionPassword = globalEncryptionPassword;
        this.globalEncryptionSalt = globalEncryptionSalt;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public String getBlackDuckProviderUrl() {
        return blackDuckProviderUrl;
    }

    public Integer getBlackDuckConnectionTimeout() {
        return blackDuckConnectionTimeout;
    }

    public String getBlackDuckApiToken() {
        return blackDuckApiToken;
    }

    public String getGlobalEncryptionPassword() {
        return globalEncryptionPassword;
    }

    public String getGlobalEncryptionSalt() {
        return globalEncryptionSalt;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }
}
