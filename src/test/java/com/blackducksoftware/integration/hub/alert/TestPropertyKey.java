package com.blackducksoftware.integration.hub.alert;

public enum TestPropertyKey {
    TEST_HUB_SERVER_URL("blackduck.hub.url"),
    TEST_USERNAME("blackduck.hub.username"),
    TEST_PASSWORD("blackduck.hub.password"),
    TEST_TRUST_HTTPS_CERT("blackduck.hub.trust.cert"),
    TEST_HUB_TIMEOUT("blackduck.hub.timeout");

    private final String propertyKey;

    private TestPropertyKey(final String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return this.propertyKey;
    }
}
