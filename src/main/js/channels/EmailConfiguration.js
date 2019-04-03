import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import CheckboxInput from 'field/input/CheckboxInput';
import NumberInput from 'field/input/NumberInput';
import PasswordInput from 'field/input/PasswordInput';
import TextInput from 'field/input/TextInput';
import ConfigButtons from 'component/common/ConfigButtons';
import ConfigurationLabel from 'component/common/ConfigurationLabel';

import { deleteConfig, getEmailConfig, updateEmailConfig } from 'store/actions/emailConfig';
import ChannelTestModal from 'component/common/ChannelTestModal';
import CollapsiblePane from 'component/common/CollapsiblePane';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import * as DescriptorUtilities from 'util/descriptorUtilities';

const ID_KEY = 'id';

// Javamail Keys
const JAVAMAIL_USER_KEY = 'mail.smtp.user';
const JAVAMAIL_HOST_KEY = 'mail.smtp.host';
const JAVAMAIL_PORT_KEY = 'mail.smtp.port';
const JAVAMAIL_CONNECTION_TIMEOUT_KEY = 'mail.smtp.connectiontimeout';
const JAVAMAIL_TIMEOUT_KEY = 'mail.smtp.timeout';
const JAVAMAIL_WRITETIMEOUT_KEY = 'mail.smtp.writetimeout';
const JAVAMAIL_FROM_KEY = 'mail.smtp.from';
const JAVAMAIL_LOCALHOST_KEY = 'mail.smtp.localhost';
const JAVAMAIL_LOCALHOST_ADDRESS_KEY = 'mail.smtp.localaddress';
const JAVAMAIL_LOCALHOST_PORT_KEY = 'mail.smtp.localport';
const JAVAMAIL_EHLO_KEY = 'mail.smtp.ehlo';
const JAVAMAIL_AUTH_KEY = 'mail.smtp.auth';
const JAVAMAIL_AUTH_MECHANISMS_KEY = 'mail.smtp.auth.mechanisms';
const JAVAMAIL_AUTH_LOGIN_DISABLE_KEY = 'mail.smtp.auth.login.disable';
const JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY = 'mail.smtp.auth.plain.disable';
const JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY = 'mail.smtp.auth.digest-md5.disable';
const JAVAMAIL_AUTH_NTLM_DISABLE_KEY = 'mail.smtp.auth.ntlm.disable';
const JAVAMAIL_AUTH_NTLM_DOMAIN_KEY = 'mail.smtp.auth.ntlm.domain';
const JAVAMAIL_AUTH_NTLM_FLAGS_KEY = 'mail.smtp.auth.ntlm.flags';
const JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY = 'mail.smtp.auth.xoauth2.disable';
const JAVAMAIL_SUBMITTER_KEY = 'mail.smtp.submitter';
const JAVAMAIL_DSN_NOTIFY_KEY = 'mail.smtp.dsn.notify';
const JAVAMAIL_DSN_RET_KEY = 'mail.smtp.dsn.ret';
const JAVAMAIL_ALLOW_8_BITMIME_KEY = 'mail.smtp.allow8bitmime';
const JAVAMAIL_SEND_PARTIAL_KEY = 'mail.smtp.sendpartial';
const JAVAMAIL_SASL_ENABLE_KEY = 'mail.smtp.sasl.enable';
const JAVAMAIL_SASL_MECHANISMS_KEY = 'mail.smtp.sasl.mechanisms';
const JAVAMAIL_SASL_AUTHORIZATION_ID_KEY = 'mail.smtp.sasl.authorizationid';
const JAVAMAIL_SASL_REALM_KEY = 'mail.smtp.sasl.realm';
const JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY = 'mail.smtp.sasl.usecanonicalhostname';
const JAVAMAIL_QUITWAIT_KEY = 'mail.smtp.quitwait';
const JAVAMAIL_REPORT_SUCCESS_KEY = 'mail.smtp.reportsuccess';
const JAVAMAIL_SSL_ENABLE_KEY = 'mail.smtp.ssl.enable';
const JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY = 'mail.smtp.ssl.checkserveridentity';
const JAVAMAIL_SSL_TRUST_KEY = 'mail.smtp.ssl.trust';
const JAVAMAIL_SSL_PROTOCOLS_KEY = 'mail.smtp.ssl.protocols';
const JAVAMAIL_SSL_CIPHERSUITES_KEY = 'mail.smtp.ssl.ciphersuites';
const JAVAMAIL_STARTTLS_ENABLE_KEY = 'mail.smtp.starttls.enable';
const JAVAMAIL_STARTTLS_REQUIRED_KEY = 'mail.smtp.starttls.required';
const JAVAMAIL_PROXY_HOST_KEY = 'mail.smtp.proxy.host';
const JAVAMAIL_PROXY_PORT_KEY = 'mail.smtp.proxy.port';
const JAVAMAIL_SOCKS_HOST_KEY = 'mail.smtp.socks.host';
const JAVAMAIL_SOCKS_PORT_KEY = 'mail.smtp.socks.port';
const JAVAMAIL_MAILEXTENSION_KEY = 'mail.smtp.mailextension';
const JAVAMAIL_USERSET_KEY = 'mail.smtp.userset';
const JAVAMAIL_NOOP_STRICT_KEY = 'mail.smtp.noop.strict';
const JAVAMAIL_PASSWORD_KEY = 'mail.smtp.password';

const fieldDescriptions = {
    [JAVAMAIL_HOST_KEY]: 'The host name of the SMTP email server.',
    [JAVAMAIL_FROM_KEY]: 'The email address to use as the return address.',
    [JAVAMAIL_AUTH_KEY]: 'Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password.',
    [JAVAMAIL_USER_KEY]: 'The username to authenticate with the SMTP server.',
    [JAVAMAIL_PASSWORD_KEY]: 'The password to authenticate with the SMTP server.',
    [JAVAMAIL_PORT_KEY]: 'The SMTP server port to connect to.',
    [JAVAMAIL_CONNECTION_TIMEOUT_KEY]: 'Socket connection timeout value in milliseconds.',
    [JAVAMAIL_TIMEOUT_KEY]: 'Socket read timeout value in milliseconds.',
    [JAVAMAIL_WRITETIMEOUT_KEY]: 'Socket write timeout value in milliseconds.',
    [JAVAMAIL_LOCALHOST_KEY]: 'Local host name used in the SMTP HELO or EHLO command.',
    [JAVAMAIL_LOCALHOST_ADDRESS_KEY]: 'Local address (host name) to bind to when creating the SMTP socket.',
    [JAVAMAIL_LOCALHOST_PORT_KEY]: 'Local port number to bind to when creating the SMTP socket.',
    [JAVAMAIL_EHLO_KEY]: 'If false, do not attempt to sign on with the EHLO command.',
    [JAVAMAIL_AUTH_MECHANISMS_KEY]: 'If set, lists the authentication mechanisms to consider, and the order in which to consider them. Only mechanisms supported by the server and supported by the current implementation will be used.',
    [JAVAMAIL_AUTH_LOGIN_DISABLE_KEY]: 'If true, prevents use of the AUTH LOGIN command.',
    [JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY]: 'If true, prevents use of the AUTH PLAIN command.',
    [JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY]: 'If true, prevents use of the AUTH DIGEST-MD5 command.',
    [JAVAMAIL_AUTH_NTLM_DISABLE_KEY]: 'If true, prevents use of the AUTH NTLM command.',
    [JAVAMAIL_AUTH_NTLM_DOMAIN_KEY]: 'The NTLM authentication domain.',
    [JAVAMAIL_AUTH_NTLM_FLAGS_KEY]: 'NTLM protocol-specific flags.',
    [JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY]: 'If true, prevents use of the AUTHENTICATE XOAUTH2 command.',
    [JAVAMAIL_SUBMITTER_KEY]: 'The submitter to use in the AUTH tag in the MAIL FROM command. Typically used by a mail relay to pass along information about the original submitter of the message',
    [JAVAMAIL_DSN_NOTIFY_KEY]: 'The NOTIFY option to the RCPT command.',
    [JAVAMAIL_DSN_RET_KEY]: 'The RET option to the MAIL command.',
    [JAVAMAIL_ALLOW_8_BITMIME_KEY]: 'If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64" encodings are converted to use "8bit" encoding',
    [JAVAMAIL_SEND_PARTIAL_KEY]: 'If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException.',
    [JAVAMAIL_SASL_ENABLE_KEY]: 'If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login.',
    [JAVAMAIL_SASL_MECHANISMS_KEY]: 'A space or comma separated list of SASL mechanism names to try to use.',
    [JAVAMAIL_SASL_AUTHORIZATION_ID_KEY]: 'The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used.',
    [JAVAMAIL_SASL_REALM_KEY]: 'The realm to use with DIGEST-MD5 authentication.',
    [JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY]: 'If set to true, the canonical host name returned by InetAddress.getCanonicalHostName is passed to the SASL mechanism, instead of the host name used to connect.',
    [JAVAMAIL_QUITWAIT_KEY]: 'If set to false, the QUIT command is sent and the connection is immediately closed.',
    [JAVAMAIL_REPORT_SUCCESS_KEY]: 'If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful.',
    [JAVAMAIL_SSL_ENABLE_KEY]: 'If set to true, use SSL to connect and use the SSL port by default.',
    [JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY]: 'If set to true, check the server identity as specified by RFC 2595.',
    [JAVAMAIL_SSL_TRUST_KEY]: 'If set, and a socket factory hasnt been specified, enables use of a MailSSLSocketFactory. If set to "*", all hosts are trusted.',
    [JAVAMAIL_SSL_PROTOCOLS_KEY]: 'Specifies the SSL protocols that will be enabled for SSL connections.',
    [JAVAMAIL_SSL_CIPHERSUITES_KEY]: 'Specifies the SSL cipher suites that will be enabled for SSL connections.',
    [JAVAMAIL_STARTTLS_ENABLE_KEY]: 'If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. If the server does not support STARTTLS, the connection continues without the use of TLS.',
    [JAVAMAIL_STARTTLS_REQUIRED_KEY]: 'If true, requires the use of the STARTTLS command. If the server doesnt support the STARTTLS command, or the command fails, the connect method will fail.',
    [JAVAMAIL_PROXY_HOST_KEY]: 'Specifies the host name of an HTTP web proxy server that will be used for connections to the mail server.',
    [JAVAMAIL_PROXY_PORT_KEY]: 'Specifies the port number for the HTTP web proxy server.',
    [JAVAMAIL_SOCKS_HOST_KEY]: 'Specifies the host name of a SOCKS5 proxy server that will be used for connections to the mail server.',
    [JAVAMAIL_SOCKS_PORT_KEY]: 'Specifies the port number for the SOCKS5 proxy server.',
    [JAVAMAIL_MAILEXTENSION_KEY]: 'Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions.',
    [JAVAMAIL_USERSET_KEY]: 'If set to true, use the RSET command instead of the NOOP command in the isConnected method.',
    [JAVAMAIL_NOOP_STRICT_KEY]: 'If set to true, insist on a 250 response code from the NOOP command to indicate success.'
};

const fieldNames = [
    ID_KEY,
    JAVAMAIL_USER_KEY,
    JAVAMAIL_HOST_KEY,
    JAVAMAIL_PORT_KEY,
    JAVAMAIL_CONNECTION_TIMEOUT_KEY,
    JAVAMAIL_TIMEOUT_KEY,
    JAVAMAIL_WRITETIMEOUT_KEY,
    JAVAMAIL_FROM_KEY,
    JAVAMAIL_LOCALHOST_KEY,
    JAVAMAIL_LOCALHOST_ADDRESS_KEY,
    JAVAMAIL_LOCALHOST_PORT_KEY,
    JAVAMAIL_EHLO_KEY,
    JAVAMAIL_AUTH_KEY,
    JAVAMAIL_AUTH_MECHANISMS_KEY,
    JAVAMAIL_AUTH_LOGIN_DISABLE_KEY,
    JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY,
    JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY,
    JAVAMAIL_AUTH_NTLM_DISABLE_KEY,
    JAVAMAIL_AUTH_NTLM_DOMAIN_KEY,
    JAVAMAIL_AUTH_NTLM_FLAGS_KEY,
    JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY,
    JAVAMAIL_SUBMITTER_KEY,
    JAVAMAIL_DSN_NOTIFY_KEY,
    JAVAMAIL_DSN_RET_KEY,
    JAVAMAIL_ALLOW_8_BITMIME_KEY,
    JAVAMAIL_SEND_PARTIAL_KEY,
    JAVAMAIL_SASL_ENABLE_KEY,
    JAVAMAIL_SASL_MECHANISMS_KEY,
    JAVAMAIL_SASL_AUTHORIZATION_ID_KEY,
    JAVAMAIL_SASL_REALM_KEY,
    JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY,
    JAVAMAIL_QUITWAIT_KEY,
    JAVAMAIL_REPORT_SUCCESS_KEY,
    JAVAMAIL_SSL_ENABLE_KEY,
    JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY,
    JAVAMAIL_SSL_TRUST_KEY,
    JAVAMAIL_SSL_PROTOCOLS_KEY,
    JAVAMAIL_SSL_CIPHERSUITES_KEY,
    JAVAMAIL_STARTTLS_ENABLE_KEY,
    JAVAMAIL_STARTTLS_REQUIRED_KEY,
    JAVAMAIL_PROXY_HOST_KEY,
    JAVAMAIL_PROXY_PORT_KEY,
    JAVAMAIL_SOCKS_HOST_KEY,
    JAVAMAIL_SOCKS_PORT_KEY,
    JAVAMAIL_MAILEXTENSION_KEY,
    JAVAMAIL_USERSET_KEY,
    JAVAMAIL_NOOP_STRICT_KEY,
    JAVAMAIL_PASSWORD_KEY
];

const configurationDescription = 'This page allows you to configure the email server that Alert will send emails to.';

class EmailConfiguration extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentEmailConfig: FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL)
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.props.getEmailConfig();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentEmailConfig !== prevProps.currentEmailConfig && this.props.updateStatus === 'DELETED') {
            const newState = FieldModelUtilities.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL);
            this.setState({
                currentEmailConfig: newState
            });
        } else if (this.props.currentEmailConfig !== prevProps.currentEmailConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const newState = FieldModelUtilities.checkModelOrCreateEmpty(this.props.currentEmailConfig, fieldNames);
            this.setState({
                currentEmailConfig: newState
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
        const newState = FieldModelUtilities.updateFieldModelSingleValue(this.state.currentEmailConfig, target.name, value);
        this.setState({
            currentEmailConfig: newState
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        const fieldModel = this.state.currentEmailConfig;
        const emptyModel = !FieldModelUtilities.hasAnyValuesExcludingId(fieldModel);
        const id = FieldModelUtilities.getFieldModelId(fieldModel);
        if (emptyModel && id) {
            this.props.deleteConfig(id);
        } else {
            this.props.updateEmailConfig(fieldModel);
        }
    }

    render() {
        const fieldModel = this.state.currentEmailConfig;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <ConfigurationLabel fontAwesomeIcon="envelope" configurationName="Email" description={configurationDescription} />
                {errorMessage && <div className="alert alert-danger">
                    {errorMessage}
                </div>}

                {actionMessage && <div className="alert alert-success">
                    {actionMessage}
                </div>}

                <form className="form-horizontal" onSubmit={this.handleSubmit} noValidate={true}>
                    <TextInput
                        id={JAVAMAIL_HOST_KEY}
                        label="SMTP Host"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_HOST_KEY)}
                        name={JAVAMAIL_HOST_KEY}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_HOST_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_HOST_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_HOST_KEY]}
                    />

                    <TextInput
                        id={JAVAMAIL_FROM_KEY}
                        label="SMTP From"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_FROM_KEY)}
                        name={JAVAMAIL_FROM_KEY}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_FROM_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_FROM_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_FROM_KEY]}
                    />

                    <CheckboxInput
                        id={JAVAMAIL_AUTH_KEY}
                        label="SMTP Auth"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_KEY)}
                        name={JAVAMAIL_AUTH_KEY}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_KEY]}
                    />

                    <TextInput
                        id={JAVAMAIL_USER_KEY}
                        label="SMTP User"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_USER_KEY)}
                        name={JAVAMAIL_USER_KEY}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_USER_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_USER_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_USER_KEY]}
                    />

                    <PasswordInput
                        id={JAVAMAIL_PASSWORD_KEY}
                        label="SMTP Password"
                        description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_PASSWORD_KEY)}
                        name={JAVAMAIL_PASSWORD_KEY}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_PASSWORD_KEY)}
                        isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, JAVAMAIL_PASSWORD_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_PASSWORD_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_PASSWORD_KEY]}
                    />
                    <CollapsiblePane
                        title="Advanced Settings"
                        expanded={() => FieldModelUtilities.keysHaveValueOrIsSet(fieldModel, [
                            JAVAMAIL_PORT_KEY,
                            JAVAMAIL_CONNECTION_TIMEOUT_KEY,
                            JAVAMAIL_TIMEOUT_KEY,
                            JAVAMAIL_WRITETIMEOUT_KEY,
                            JAVAMAIL_LOCALHOST_KEY,
                            JAVAMAIL_LOCALHOST_ADDRESS_KEY,
                            JAVAMAIL_LOCALHOST_PORT_KEY,
                            JAVAMAIL_EHLO_KEY,
                            JAVAMAIL_AUTH_MECHANISMS_KEY,
                            JAVAMAIL_AUTH_LOGIN_DISABLE_KEY,
                            JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY,
                            JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY,
                            JAVAMAIL_AUTH_NTLM_DISABLE_KEY,
                            JAVAMAIL_AUTH_NTLM_DOMAIN_KEY,
                            JAVAMAIL_AUTH_NTLM_FLAGS_KEY,
                            JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY,
                            JAVAMAIL_SUBMITTER_KEY,
                            JAVAMAIL_DSN_NOTIFY_KEY,
                            JAVAMAIL_DSN_RET_KEY,
                            JAVAMAIL_ALLOW_8_BITMIME_KEY,
                            JAVAMAIL_SEND_PARTIAL_KEY,
                            JAVAMAIL_SASL_ENABLE_KEY,
                            JAVAMAIL_SASL_MECHANISMS_KEY,
                            JAVAMAIL_SASL_AUTHORIZATION_ID_KEY,
                            JAVAMAIL_SASL_REALM_KEY,
                            JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY,
                            JAVAMAIL_QUITWAIT_KEY,
                            JAVAMAIL_REPORT_SUCCESS_KEY,
                            JAVAMAIL_SSL_ENABLE_KEY,
                            JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY,
                            JAVAMAIL_SSL_TRUST_KEY,
                            JAVAMAIL_SSL_PROTOCOLS_KEY,
                            JAVAMAIL_SSL_CIPHERSUITES_KEY,
                            JAVAMAIL_STARTTLS_ENABLE_KEY,
                            JAVAMAIL_STARTTLS_REQUIRED_KEY,
                            JAVAMAIL_PROXY_HOST_KEY,
                            JAVAMAIL_PROXY_PORT_KEY,
                            JAVAMAIL_SOCKS_HOST_KEY,
                            JAVAMAIL_SOCKS_PORT_KEY,
                            JAVAMAIL_MAILEXTENSION_KEY,
                            JAVAMAIL_USERSET_KEY,
                            JAVAMAIL_NOOP_STRICT_KEY])}
                    >
                        <NumberInput
                            id={JAVAMAIL_PORT_KEY}
                            label="SMTP Port"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_PORT_KEY)}
                            name={JAVAMAIL_PORT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PORT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_CONNECTION_TIMEOUT_KEY}
                            label="SMTP Connection Timeout"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            name={JAVAMAIL_CONNECTION_TIMEOUT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_CONNECTION_TIMEOUT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_TIMEOUT_KEY}
                            label="SMTP Timeout"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_TIMEOUT_KEY)}
                            name={JAVAMAIL_TIMEOUT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_TIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_TIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_TIMEOUT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_WRITETIMEOUT_KEY}
                            label="SMTP Write Timeout"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_WRITETIMEOUT_KEY)}
                            name={JAVAMAIL_WRITETIMEOUT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_WRITETIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_WRITETIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_WRITETIMEOUT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_LOCALHOST_KEY}
                            label="SMTP Localhost"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_LOCALHOST_KEY)}
                            name={JAVAMAIL_LOCALHOST_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_LOCALHOST_ADDRESS_KEY}
                            label="SMTP Local Address"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            name={JAVAMAIL_LOCALHOST_ADDRESS_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_ADDRESS_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_LOCALHOST_PORT_KEY}
                            label="SMTP Local Port"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_LOCALHOST_PORT_KEY)}
                            name={JAVAMAIL_LOCALHOST_PORT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_PORT_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_EHLO_KEY}
                            label="SMTP Ehlo"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_EHLO_KEY)}
                            name={JAVAMAIL_EHLO_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_EHLO_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_EHLO_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_EHLO_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_AUTH_MECHANISMS_KEY}
                            label="SMTP Auth Mechanisms"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            name={JAVAMAIL_AUTH_MECHANISMS_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_MECHANISMS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_LOGIN_DISABLE_KEY}
                            label="SMTP Auth Login Disable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            name={JAVAMAIL_AUTH_LOGIN_DISABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_LOGIN_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY}
                            label="SMTP Auth Plain Disable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            name={JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY}
                            label="SMTP Auth Digest MD5 Disable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            name={JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_NTLM_DISABLE_KEY}
                            label="SMTP Auth NTLM Disable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            name={JAVAMAIL_AUTH_NTLM_DISABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_DISABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_AUTH_NTLM_DOMAIN_KEY}
                            label="SMTP Auth NTLM Domain"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            name={JAVAMAIL_AUTH_NTLM_DOMAIN_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_DOMAIN_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_AUTH_NTLM_FLAGS_KEY}
                            label="SMTP Auth NTLM Flags"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            name={JAVAMAIL_AUTH_NTLM_FLAGS_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_FLAGS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY}
                            label="SMTP Auth XOAuth2 Disable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            name={JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SUBMITTER_KEY}
                            label="SMTP Submitter"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SUBMITTER_KEY)}
                            name={JAVAMAIL_SUBMITTER_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SUBMITTER_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SUBMITTER_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SUBMITTER_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_DSN_NOTIFY_KEY}
                            label="SMTP DNS Notify"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_DSN_NOTIFY_KEY)}
                            name={JAVAMAIL_DSN_NOTIFY_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_DSN_NOTIFY_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_DSN_NOTIFY_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_DSN_NOTIFY_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_DSN_RET_KEY}
                            label="SMTP DNS Ret"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_DSN_RET_KEY)}
                            name={JAVAMAIL_DSN_RET_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_DSN_RET_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_DSN_RET_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_DSN_RET_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_ALLOW_8_BITMIME_KEY}
                            label="SMTP Allow 8-bit Mime"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            name={JAVAMAIL_ALLOW_8_BITMIME_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_ALLOW_8_BITMIME_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SEND_PARTIAL_KEY}
                            label="SMTP Send Partial"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SEND_PARTIAL_KEY)}
                            name={JAVAMAIL_SEND_PARTIAL_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SEND_PARTIAL_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SEND_PARTIAL_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SEND_PARTIAL_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SASL_ENABLE_KEY}
                            label="SMTP SASL Enable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SASL_ENABLE_KEY)}
                            name={JAVAMAIL_SASL_ENABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SASL_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SASL_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_ENABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_MECHANISMS_KEY}
                            label="SMTP SASL Mechanisms"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SASL_MECHANISMS_KEY)}
                            name={JAVAMAIL_SASL_MECHANISMS_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_MECHANISMS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SASL_MECHANISMS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_MECHANISMS_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_AUTHORIZATION_ID_KEY}
                            label="SMTP SASL Authorization ID"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            name={JAVAMAIL_SASL_AUTHORIZATION_ID_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_AUTHORIZATION_ID_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_REALM_KEY}
                            label="SMTP SASL Realm"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SASL_REALM_KEY)}
                            name={JAVAMAIL_SASL_REALM_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_REALM_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SASL_REALM_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_REALM_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY}
                            label="SMTP SASL Use Canonical Hostname"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            name={JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_QUITWAIT_KEY}
                            label="SMTP QuitWait"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_QUITWAIT_KEY)}
                            name={JAVAMAIL_QUITWAIT_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_QUITWAIT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_QUITWAIT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_QUITWAIT_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_REPORT_SUCCESS_KEY}
                            label="SMTP Report Success"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_REPORT_SUCCESS_KEY)}
                            name={JAVAMAIL_REPORT_SUCCESS_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_REPORT_SUCCESS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_REPORT_SUCCESS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_REPORT_SUCCESS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SSL_ENABLE_KEY}
                            label="SMTP SSL Enable"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SSL_ENABLE_KEY)}
                            name={JAVAMAIL_SSL_ENABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SSL_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SSL_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_ENABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY}
                            label="SMTP SSL Check Server Identity"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            name={JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_TRUST_KEY}
                            label="SMTP SSL Trust"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SSL_TRUST_KEY)}
                            name={JAVAMAIL_SSL_TRUST_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_TRUST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SSL_TRUST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_TRUST_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_PROTOCOLS_KEY}
                            label="SMTP SSL Protocols"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            name={JAVAMAIL_SSL_PROTOCOLS_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_PROTOCOLS_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_CIPHERSUITES_KEY}
                            label="SMTP SSL Cipher Suites"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            name={JAVAMAIL_SSL_CIPHERSUITES_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_CIPHERSUITES_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_STARTTLS_ENABLE_KEY}
                            label="SMTP Start TLS Enabled"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            name={JAVAMAIL_STARTTLS_ENABLE_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_STARTTLS_ENABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_STARTTLS_REQUIRED_KEY}
                            label="SMTP Start TLS Required"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            name={JAVAMAIL_STARTTLS_REQUIRED_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_STARTTLS_REQUIRED_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_PROXY_HOST_KEY}
                            label="SMTP Proxy Host"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_PROXY_HOST_KEY)}
                            name={JAVAMAIL_PROXY_HOST_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_PROXY_HOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_PROXY_HOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PROXY_HOST_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_PROXY_PORT_KEY}
                            label="SMTP Proxy Port"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_PROXY_PORT_KEY)}
                            name={JAVAMAIL_PROXY_PORT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_PROXY_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_PROXY_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PROXY_PORT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SOCKS_HOST_KEY}
                            label="SMTP Socks Host"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SOCKS_HOST_KEY)}
                            name={JAVAMAIL_SOCKS_HOST_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SOCKS_HOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SOCKS_HOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SOCKS_HOST_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_SOCKS_PORT_KEY}
                            label="SMTP Socks Port"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_SOCKS_PORT_KEY)}
                            name={JAVAMAIL_SOCKS_PORT_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_SOCKS_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_SOCKS_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SOCKS_PORT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_MAILEXTENSION_KEY}
                            label="SMTP Mail Extension"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_MAILEXTENSION_KEY)}
                            name={JAVAMAIL_MAILEXTENSION_KEY}
                            value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, JAVAMAIL_MAILEXTENSION_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_MAILEXTENSION_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_MAILEXTENSION_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_USERSET_KEY}
                            label="SMTP Use RSET"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_USERSET_KEY)}
                            name={JAVAMAIL_USERSET_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_USERSET_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_USERSET_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_USERSET_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_NOOP_STRICT_KEY}
                            label="SMTP NoOp Strict"
                            description={FieldModelUtilities.getFieldDescription(fieldDescriptions, JAVAMAIL_NOOP_STRICT_KEY)}
                            name={JAVAMAIL_NOOP_STRICT_KEY}
                            isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, JAVAMAIL_NOOP_STRICT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtilities.createFieldModelErrorKey(JAVAMAIL_NOOP_STRICT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_NOOP_STRICT_KEY]}
                        />
                    </CollapsiblePane>
                    <ConfigButtons
                        cancelId="email-cancel"
                        submitId="email-submit"
                        includeSave
                        includeTest
                        onTestClick={(event) => {
                            event.preventDefault();
                            this.props.openEmailConfigTest();
                        }}
                    />
                    <div>
                        <ChannelTestModal
                            destinationName="Email address"
                            showTestModal={this.props.showTestModal}
                            cancelTestModal={this.props.closeEmailConfigTest}
                            sendTestMessage={(destination) => {
                                this.props.sendEmailConfigTest(this.state.currentEmailConfig, destination);
                            }}
                            modalTesting={this.props.modalTesting}
                        />
                    </div>
                </form>
            </div>
        );
    }
}

EmailConfiguration.propTypes = {
    updateEmailConfig: PropTypes.func.isRequired,
    openEmailConfigTest: PropTypes.func.isRequired,
    closeEmailConfigTest: PropTypes.func.isRequired,
    sendEmailConfigTest: PropTypes.func.isRequired,
    deleteConfig: PropTypes.func.isRequired,
    currentEmailConfig: PropTypes.object,
    showTestModal: PropTypes.bool.isRequired,
    modalTesting: PropTypes.bool.isRequired,
    getEmailConfig: PropTypes.func.isRequired,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    actionMessage: PropTypes.string,
    fieldErrors: PropTypes.object
};

EmailConfiguration.defaultProps = {
    currentEmailConfig: {},
    errorMessage: '',
    updateStatus: '',
    actionMessage: '',
    fieldErrors: {}
};

const mapStateToProps = state => ({
    currentEmailConfig: state.emailConfig.config,
    errorMessage: state.emailConfig.error.message,
    fieldErrors: state.emailConfig.error.fieldErrors,
    updateStatus: state.emailConfig.updateStatus,
    actionMessage: state.emailConfig.actionMessage,
    showTestModal: state.emailConfig.showTestModal,
    modalTesting: state.emailConfig.modalTesting
});

const mapDispatchToProps = dispatch => ({
    getEmailConfig: () => dispatch(getEmailConfig()),
    updateEmailConfig: config => dispatch(updateEmailConfig(config)),
    sendEmailConfigTest: (config, destination) => dispatch(sendEmailConfigTest(config, destination)),
    openEmailConfigTest: () => dispatch(openEmailConfigTest()),
    closeEmailConfigTest: () => dispatch(closeEmailConfigTest()),
    deleteConfig: id => dispatch(deleteConfig(id))
});

export default connect(mapStateToProps, mapDispatchToProps)(EmailConfiguration);
