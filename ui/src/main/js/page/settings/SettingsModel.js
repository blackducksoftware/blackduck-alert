export const SETTINGS_INFO = {
    key: 'component_settings',
    url: 'settings',
    label: 'Settings'
};

export const SETTINGS_FIELD_KEYS = {
    encryptionPassword: 'settings.encryption.password',
    encryptionGlobalSalt: 'settings.encryption.global.salt',
    proxyHost: 'settings.proxy.host',
    proxyPort: 'settings.proxy.port',
    proxyUsername: 'settings.proxy.username',
    proxyPassword: 'settings.proxy.password',
    proxyNonProxyHosts: 'settings.proxy.non.proxy.hosts'
};

export const SETTINGS_PROXY_TEST_FIELD = {
    key: 'test.field.target.url',
    label: 'Target URL',
    description: 'The URL to ping using the proxy configuration.'
};
