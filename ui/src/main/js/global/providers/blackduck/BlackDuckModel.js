export const BLACKDUCK_INFO = {
    key: 'provider_blackduck',
    url: 'blackduck',
    label: 'Black Duck',
    description: 'This is the configuration to connect to the Black Duck server. Configuring this will cause Alert to start pulling data from Black Duck.'
};

export const BLACKDUCK_GLOBAL_FIELD_KEYS = {
    enabled: 'provider.common.config.enabled',
    name: 'provider.common.config.name',
    url: 'blackduck.url',
    apiKey: 'blackduck.api.key',
    timeout: 'blackduck.timeout'
};

export const BLACKDUCK_DISTRIBUTION_FIELD_KEYS = {
    policyFilter: 'blackduck.policy.notification.filter',
    vulnerabilityFilter: 'blackduck.vulnerability.notification.filter'
};

export const BLACKDUCK_URLS = {
    blackDuckConfigUrl: '/alert/providers/blackduck/edit',
    blackDuckConfigCopyUrl: '/alert/providers/blackduck/copy',
    blackDuckTableUrl: '/alert/providers/blackduck'
};
