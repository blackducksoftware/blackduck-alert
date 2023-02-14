export const AUTHENTICATION_INFO = {
    key: 'component_authentication',
    url: 'authentication',
    label: 'Authentication'
};

export const AUTHENTICATION_LDAP_GLOBAL_FIELD_KEYS = {
    enabled: 'enabled',
    serverName: 'serverName',
    managerDn: 'managerDn',
    managerPassword: 'managerPassword',
    authenticationType: 'authenticationType',
    referral: 'referral',
    userSearchBase: 'userSearchBase',
    userSearchFilter: 'userSearchFilter',
    userDnPatterns: 'userDnPatterns',
    userAttributes: 'userAttributes',
    groupSearchBase: 'groupSearchBase',
    groupSearchFilter: 'groupSearchFilter',
    groupRoleAttribute: 'groupRoleAttribute'
};

export const AUTHENTICATION_LDAP_GLOBAL_TEST_FIELD_KEYS = {
    testLDAPUsername: 'testLDAPUsername',
    testLDAPPassword: 'testLDAPPassword'
};

export const AUTHENTICATION_SAML_GLOBAL_FIELD_KEYS = {
    enabled: 'enabled',
    encryptionCertFilePath: 'encryptionCertFilePath',
    encryptionPrivateKeyFilePath: 'encryptionPrivateKeyFilePath',
    forceAuth: 'forceAuth',
    metadataFilePath: 'metadataFilePath',
    metadataMode: 'metadataMode',
    metadataUrl: 'metadataUrl',
    name: 'name',
    signingCertFilePath: 'signingCertFilePath',
    signingPrivateKeyFilePath: 'signingPrivateKeyFilePath',
    verificationCertFilePath: 'verificationCertFilePath',
    wantAssertionsSigned: 'wantAssertionsSigned'
};
