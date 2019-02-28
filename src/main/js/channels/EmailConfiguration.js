import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import CheckboxInput from 'field/input/CheckboxInput';
import NumberInput from 'field/input/NumberInput';
import PasswordInput from 'field/input/PasswordInput';
import TextInput from 'field/input/TextInput';
import ConfigButtons from 'component/common/ConfigButtons';

import { closeEmailConfigTest, getEmailConfig, openEmailConfigTest, sendEmailConfigTest, updateEmailConfig } from 'store/actions/emailConfig';
import ChannelTestModal from 'component/common/ChannelTestModal';
import CollapsiblePane from 'component/common/CollapsiblePane';
import * as FieldModelUtil from 'util/fieldModelUtilities';
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

class EmailConfiguration extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            currentEmailConfig: FieldModelUtil.createEmptyFieldModel(fieldNames, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, DescriptorUtilities.DESCRIPTOR_NAME.CHANNEL_EMAIL)
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.props.getEmailConfig();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.props.currentEmailConfig !== prevProps.currentEmailConfig && (this.props.updateStatus === 'FETCHED' || this.props.updateStatus === 'UPDATED')) {
            const newState = FieldModelUtil.checkModelOrCreateEmpty(this.props.currentEmailConfig, fieldNames);
            this.setState({
                currentEmailConfig: newState
            });
        }
    }

    handleChange({ target }) {
        const value = target.type === 'checkbox' ? target.checked.toString() : target.value;
        const newState = FieldModelUtil.updateFieldModelSingleValue(this.state.currentEmailConfig, target.name, value);
        this.setState({
            currentEmailConfig: newState
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        const fieldModel = this.state.currentEmailConfig;
        this.props.updateEmailConfig(fieldModel);
    }

    render() {
        const fieldModel = this.state.currentEmailConfig;
        const { errorMessage, actionMessage } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-envelope" />
                    Email
                </h1>
                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    {errorMessage && <div className="alert alert-danger">
                        {errorMessage}
                    </div>}

                    {actionMessage && <div className="alert alert-success">
                        {actionMessage}
                    </div>}

                    <TextInput
                        id={JAVAMAIL_HOST_KEY}
                        label='SMTP Host'
                        description='The host name of the SMTP email server.'
                        name={JAVAMAIL_HOST_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_HOST_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_HOST_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_HOST_KEY]}
                    />

                    <TextInput
                        id={JAVAMAIL_FROM_KEY}
                        label='SMTP From'
                        description='The email address to use as the return address.'
                        name={JAVAMAIL_FROM_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_FROM_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_FROM_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_FROM_KEY]}
                    />

                    <CheckboxInput
                        id={JAVAMAIL_AUTH_KEY}
                        label='SMTP Auth'
                        description='Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password.'
                        name={JAVAMAIL_AUTH_KEY}
                        isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_KEY]}
                    />

                    <TextInput
                        id={JAVAMAIL_USER_KEY}
                        label='SMTP User'
                        description='The username to authenticate with the SMTP server.'
                        name={JAVAMAIL_USER_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_USER_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_USER_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_USER_KEY]}
                    />

                    <PasswordInput
                        id={JAVAMAIL_PASSWORD_KEY}
                        label='SMTP Password'
                        description='The password to authenticate with the SMTP server.'
                        name={JAVAMAIL_PASSWORD_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PASSWORD_KEY)}
                        isSet={FieldModelUtil.isFieldModelValueSet(fieldModel, JAVAMAIL_PASSWORD_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PASSWORD_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_PASSWORD_KEY]}
                    />
                    <CollapsiblePane title='Advanced Settings'>
                        <NumberInput
                            id={JAVAMAIL_PORT_KEY}
                            label='SMTP Port'
                            description='The SMTP server port to connect to.'
                            name={JAVAMAIL_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PORT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_CONNECTION_TIMEOUT_KEY}
                            label='SMTP Connection Timeout'
                            description='Socket connection timeout value in milliseconds.'
                            name={JAVAMAIL_CONNECTION_TIMEOUT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_CONNECTION_TIMEOUT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_TIMEOUT_KEY}
                            label='SMTP Timeout'
                            description='Socket read timeout value in milliseconds.'
                            name={JAVAMAIL_TIMEOUT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_TIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_TIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_TIMEOUT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_WRITETIMEOUT_KEY}
                            label='SMTP Write Timeout'
                            description='Socket write timeout value in milliseconds.'
                            name={JAVAMAIL_WRITETIMEOUT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_WRITETIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_WRITETIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_WRITETIMEOUT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_LOCALHOST_KEY}
                            label='SMTP Localhost'
                            description='Local host name used in the SMTP HELO or EHLO command.'
                            name={JAVAMAIL_LOCALHOST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_LOCALHOST_ADDRESS_KEY}
                            label='SMTP Local Address'
                            description='Local address (host name) to bind to when creating the SMTP socket.'
                            name={JAVAMAIL_LOCALHOST_ADDRESS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_ADDRESS_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_LOCALHOST_PORT_KEY}
                            label='SMTP Local Port'
                            description='Local port number to bind to when creating the SMTP socket.'
                            name={JAVAMAIL_LOCALHOST_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_PORT_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_EHLO_KEY}
                            label='SMTP Ehlo'
                            description='If false, do not attempt to sign on with the EHLO command.'
                            name={JAVAMAIL_EHLO_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_EHLO_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_EHLO_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_EHLO_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_AUTH_MECHANISMS_KEY}
                            label='SMTP Auth Mechanisms'
                            description='If set, lists the authentication mechanisms to consider, and the order in which to consider them. Only mechanisms supported by the server and supported by the current implementation will be used.'
                            name={JAVAMAIL_AUTH_MECHANISMS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_MECHANISMS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_LOGIN_DISABLE_KEY}
                            label='SMTP Auth Login Disable'
                            description='If true, prevents use of the AUTH LOGIN command.'
                            name={JAVAMAIL_AUTH_LOGIN_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_LOGIN_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY}
                            label='SMTP Auth Plain Disable'
                            description='If true, prevents use of the AUTH PLAIN command.'
                            name={JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY}
                            label='SMTP Auth Digest MD5 Disable'
                            description='If true, prevents use of the AUTH DIGEST-MD5 command.'
                            name={JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_NTLM_DISABLE_KEY}
                            label='SMTP Auth NTLM Disable'
                            description='If true, prevents use of the AUTH NTLM command.'
                            name={JAVAMAIL_AUTH_NTLM_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_DISABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_AUTH_NTLM_DOMAIN_KEY}
                            label='SMTP Auth NTLM Domain'
                            description='The NTLM authentication domain.'
                            name={JAVAMAIL_AUTH_NTLM_DOMAIN_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_DOMAIN_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_AUTH_NTLM_FLAGS_KEY}
                            label='SMTP Auth NTLM Flags'
                            description='NTLM protocol-specific flags.'
                            name={JAVAMAIL_AUTH_NTLM_FLAGS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_FLAGS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY}
                            label='SMTP Auth XOAuth2 Disable'
                            description='If true, prevents use of the AUTHENTICATE XOAUTH2 command.'
                            name={JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SUBMITTER_KEY}
                            label='SMTP Submitter'
                            description='The submitter to use in the AUTH tag in the MAIL FROM command. Typically used by a mail relay to pass along information about the original submitter of the message'
                            name={JAVAMAIL_SUBMITTER_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SUBMITTER_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SUBMITTER_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SUBMITTER_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_DSN_NOTIFY_KEY}
                            label='SMTP DNS Notify'
                            description='The NOTIFY option to the RCPT command.'
                            name={JAVAMAIL_DSN_NOTIFY_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_DSN_NOTIFY_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_DSN_NOTIFY_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_DSN_NOTIFY_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_DSN_RET_KEY}
                            label='SMTP DNS Ret'
                            description='The RET option to the MAIL command.'
                            name={JAVAMAIL_DSN_RET_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_DSN_RET_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_DSN_RET_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_DSN_RET_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_ALLOW_8_BITMIME_KEY}
                            label='SMTP Allow 8-bit Mime'
                            description='If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64" encodings are converted to use "8bit" encoding'
                            name={JAVAMAIL_ALLOW_8_BITMIME_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_ALLOW_8_BITMIME_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SEND_PARTIAL_KEY}
                            label='SMTP Send Partial'
                            description='If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException.'
                            name={JAVAMAIL_SEND_PARTIAL_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SEND_PARTIAL_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SEND_PARTIAL_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SEND_PARTIAL_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SASL_ENABLE_KEY}
                            label='SMTP SASL Enable'
                            description='If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login.'
                            name={JAVAMAIL_SASL_ENABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SASL_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_ENABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_MECHANISMS_KEY}
                            label='SMTP SASL Mechanisms'
                            description='A space or comma separated list of SASL mechanism names to try to use.'
                            name={JAVAMAIL_SASL_MECHANISMS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_MECHANISMS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_MECHANISMS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_MECHANISMS_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_AUTHORIZATION_ID_KEY}
                            label='SMTP SASL Authorization ID'
                            description='The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used.'
                            name={JAVAMAIL_SASL_AUTHORIZATION_ID_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_AUTHORIZATION_ID_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_REALM_KEY}
                            label='SMTP SASL Realm'
                            description='The realm to use with DIGEST-MD5 authentication.'
                            name={JAVAMAIL_SASL_REALM_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_REALM_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_REALM_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_REALM_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY}
                            label='SMTP SASL Use Canonical Hostname'
                            description='If set to true, the canonical host name returned by InetAddress.getCanonicalHostName is passed to the SASL mechanism, instead of the host name used to connect.'
                            name={JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_QUITWAIT_KEY}
                            label='SMTP QuitWait'
                            description='If set to false, the QUIT command is sent and the connection is immediately closed.'
                            name={JAVAMAIL_QUITWAIT_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_QUITWAIT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_QUITWAIT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_QUITWAIT_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_REPORT_SUCCESS_KEY}
                            label='SMTP Report Success'
                            description='If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful.'
                            name={JAVAMAIL_REPORT_SUCCESS_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_REPORT_SUCCESS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_REPORT_SUCCESS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_REPORT_SUCCESS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SSL_ENABLE_KEY}
                            label='SMTP SSL Enable'
                            description='If set to true, use SSL to connect and use the SSL port by default.'
                            name={JAVAMAIL_SSL_ENABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SSL_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_ENABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY}
                            label='SMTP SSL Check Server Identity'
                            description='If set to true, check the server identity as specified by RFC 2595.'
                            name={JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_TRUST_KEY}
                            label='SMTP SSL Trust'
                            description='If set, and a socket factory hasnt been specified, enables use of a MailSSLSocketFactory. If set to "*", all hosts are trusted.'
                            name={JAVAMAIL_SSL_TRUST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_TRUST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_TRUST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_TRUST_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_PROTOCOLS_KEY}
                            label='SMTP SSL Protocols'
                            description='Specifies the SSL protocols that will be enabled for SSL connections.'
                            name={JAVAMAIL_SSL_PROTOCOLS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_PROTOCOLS_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_CIPHERSUITES_KEY}
                            label='SMTP SSL Cipher Suites'
                            description='Specifies the SSL cipher suites that will be enabled for SSL connections.'
                            name={JAVAMAIL_SSL_CIPHERSUITES_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_CIPHERSUITES_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_STARTTLS_ENABLE_KEY}
                            label='SMTP Start TLS Enabled'
                            description='If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. If the server does not support STARTTLS, the connection continues without the use of TLS.'
                            name={JAVAMAIL_STARTTLS_ENABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_STARTTLS_ENABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_STARTTLS_REQUIRED_KEY}
                            label='SMTP Start TLS Required'
                            description='If true, requires the use of the STARTTLS command. If the server doesnt support the STARTTLS command, or the command fails, the connect method will fail.'
                            name={JAVAMAIL_STARTTLS_REQUIRED_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_STARTTLS_REQUIRED_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_PROXY_HOST_KEY}
                            label='SMTP Proxy Host'
                            description='Specifies the host name of an HTTP web proxy server that will be used for connections to the mail server.'
                            name={JAVAMAIL_PROXY_HOST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PROXY_HOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PROXY_HOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PROXY_HOST_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_PROXY_PORT_KEY}
                            label='SMTP Proxy Port'
                            description='Specifies the port number for the HTTP web proxy server.'
                            name={JAVAMAIL_PROXY_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PROXY_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PROXY_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PROXY_PORT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SOCKS_HOST_KEY}
                            label='SMTP Socks Host'
                            description='Specifies the host name of a SOCKS5 proxy server that will be used for connections to the mail server.'
                            name={JAVAMAIL_SOCKS_HOST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SOCKS_HOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SOCKS_HOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SOCKS_HOST_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_SOCKS_PORT_KEY}
                            label='SMTP Socks Port'
                            description='Specifies the port number for the SOCKS5 proxy server.'
                            name={JAVAMAIL_SOCKS_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SOCKS_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SOCKS_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SOCKS_PORT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_MAILEXTENSION_KEY}
                            label='SMTP Mail Extension'
                            description='Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions.'
                            name={JAVAMAIL_MAILEXTENSION_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_MAILEXTENSION_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_MAILEXTENSION_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_MAILEXTENSION_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_USERSET_KEY}
                            label='SMTP Use RSET'
                            description='If set to true, use the RSET command instead of the NOOP command in the isConnected method.'
                            name={JAVAMAIL_USERSET_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_USERSET_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_USERSET_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_USERSET_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_NOOP_STRICT_KEY}
                            label='SMTP NoOp Strict'
                            description='If set to true, insist on a 250 response code from the NOOP command to indicate success.'
                            name={JAVAMAIL_NOOP_STRICT_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_NOOP_STRICT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_NOOP_STRICT_KEY)}
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
    currentEmailConfig: PropTypes.object,
    showTestModal: PropTypes.bool.isRequired,
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
    showTestModal: state.emailConfig.showTestModal
});

const mapDispatchToProps = dispatch => ({
    getEmailConfig: () => dispatch(getEmailConfig()),
    updateEmailConfig: config => dispatch(updateEmailConfig(config)),
    sendEmailConfigTest: (config, destination) => dispatch(sendEmailConfigTest(config, destination)),
    openEmailConfigTest: () => dispatch(openEmailConfigTest()),
    closeEmailConfigTest: () => dispatch(closeEmailConfigTest())
});

export default connect(mapStateToProps, mapDispatchToProps)(EmailConfiguration);
