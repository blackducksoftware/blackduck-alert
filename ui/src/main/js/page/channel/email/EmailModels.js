import { createTableSelectColumn } from 'common/input/TableSelectInput';

export const EMAIL_DISTRIBUTION_FIELD_KEYS = {
    additionalAddresses: 'email.additional.addresses',
    additionalAddressesOnly: 'email.additional.addresses.only',
    attachmentFormat: 'email.attachment.format',
    projectOwnerOnly: 'project.owner.only',
    subject: 'email.subject.line'
};

export const EMAIL_DISTRIBUTION_ATTACHMENT_OPTIONS = [
    { label: 'None', value: 'NONE' },
    { label: 'Json', value: 'JSON' },
    { label: 'Xml', value: 'XML' },
    { label: 'Csv', value: 'CSV' }
];

export const EMAIL_DISTRIBUTION_ADDITIONAL_EMAIL_COLUMNS = [
    createTableSelectColumn('emailAddress', 'Email Address', true, true, true)
];

export const EMAIL_INFO = {
    key: 'channel_email',
    url: 'email',
    label: 'Email'
};

export const EMAIL_TEST_FIELD = {
    key: 'test.field.destination.name',
    label: 'Email address',
    description: 'The email address to send a message to.'
};

export const EMAIL_GLOBAL_FIELD_KEYS = {
    host: 'mail.smtp.host',
    from: 'mail.smtp.from',
    auth: 'mail.smtp.auth',
    user: 'mail.smtp.user',
    password: 'mail.smtp.password'
};

export const EMAIL_GLOBAL_ADVANCED_FIELD_KEYS = {
    port: 'mail.smtp.port',
    connectionTimeout: 'mail.smtp.connectiontimeout',
    timeout: 'mail.smtp.timeout',
    writeTimeout: 'mail.smtp.writetimeout',
    localhost: 'mail.smtp.localhost',
    localAddress: 'mail.smtp.localaddress',
    localPort: 'mail.smtp.localport',
    ehlo: 'mail.smtp.ehlo',
    authMechanisms: 'mail.smtp.auth.mechanisms',
    loginDisable: 'mail.smtp.auth.login.disable',
    authPlainDisable: 'mail.smtp.auth.plain.disable',
    authDigestMd5Disable: 'mail.smtp.auth.digest-md5.disable',
    authNtlmDisable: 'mail.smtp.auth.ntlm.disable',
    authNtlmDomain: 'mail.smtp.auth.ntlm.domain',
    authNtlmFlags: 'mail.smtp.auth.ntlm.flags',
    authXoauth2Disable: 'mail.smtp.auth.xoauth2.disable',
    submitter: 'mail.smtp.submitter',
    dsnNotify: 'mail.smtp.dsn.notify',
    dsnRet: 'mail.smtp.dsn.ret',
    allow8BitMime: 'mail.smtp.allow8bitmime',
    sendPartial: 'mail.smtp.sendpartial',
    saslEnable: 'mail.smtp.sasl.enable',
    saslMechanism: 'mail.smtp.sasl.mechanisms',
    saslAuthorizationId: 'mail.smtp.sasl.authorizationid',
    saslRealm: 'mail.smtp.sasl.realm',
    saslUseCanonicalHostname: 'mail.smtp.sasl.usecanonicalhostname',
    quitWait: 'mail.smtp.quitwait',
    reportSuccess: 'mail.smtp.reportsuccess',
    sslEnable: 'mail.smtp.ssl.enable',
    sslCheckServerIdentity: 'mail.smtp.ssl.checkserveridentity',
    sslTrust: 'mail.smtp.ssl.trust',
    sslProtocols: 'mail.smtp.ssl.protocols',
    sslCipherSuites: 'mail.smtp.ssl.ciphersuites',
    startTlsEnable: 'mail.smtp.starttls.enable',
    startTlsRequired: 'mail.smtp.starttls.required',
    proxyHost: 'mail.smtp.proxy.host',
    proxyPort: 'mail.smtp.proxy.port',
    socksHost: 'mail.smtp.socks.host',
    socksPort: 'mail.smtp.socks.port',
    mailExtensions: 'mail.smtp.mailextension',
    userSet: 'mail.smtp.userset',
    noopStrict: 'mail.smtp.noop.strict'
};

/*
    Other properties:
    "emailCategory",
    "content",
    "logo.image",
    "provider_url",
    "provider_name",
    "provider_project_name",
    "subject_line",
    "topicsList",
    "startDate",
    "endDate",
    "emailCategory",
 */
