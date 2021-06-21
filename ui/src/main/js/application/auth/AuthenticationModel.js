export const AUTHENTICATION_INFO = {
    key: 'component_authentication',
    url: 'authentication',
    label: 'Authentication'
};

export const AUTHENTICATION_TEST_FIELD_KEYS = {
    username: 'test.field.user.name',
    password: 'test.field.user.password',
    noInput: 'test.field.saml.no.input'
};

export const AUTHENTICATION_LDAP_FIELD_KEYS = {
    enabled: 'settings.ldap.enabled',
    server: 'settings.ldap.server',
    managerDn: 'settings.ldap.manager.dn',
    managerPassword: 'settings.ldap.manager.password',
    authenticationType: 'settings.ldap.authentication.type',
    referral: 'settings.ldap.referral',
    userSearchBase: 'settings.ldap.user.search.base',
    userSearchFilter: 'settings.ldap.user.search.filter',
    userDnPatterns: 'settings.ldap.user.dn.patterns',
    userAttributes: 'settings.ldap.user.attributes',
    groupSearchBase: 'settings.ldap.group.search.base',
    groupSearchFilter: 'settings.ldap.group.search.filter',
    groupRoleAttribute: 'settings.ldap.group.role.attribute'
};

export const AUTHENTICATION_SAML_FIELD_KEYS = {
    enabled: 'settings.saml.enabled',
    wantAssertionsSigned: 'settings.saml.want.assertions.signed',
    forceAuth: 'settings.saml.force.auth',
    metadataUrl: 'settings.saml.metadata.url',
    entityId: 'settings.saml.entity.id',
    entityBaseUrl: 'settings.saml.entity.base.url',
    metadataFile: 'settings.saml.metadata.file',
    roleAttributeMapping: 'settings.saml.role.attribute.mapping.name'
};
