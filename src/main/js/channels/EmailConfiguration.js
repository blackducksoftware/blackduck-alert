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
                        label="SMTP Host"
                        name={JAVAMAIL_HOST_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_HOST_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_HOST_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_HOST_KEY]}
                    />

                    <TextInput
                        id={JAVAMAIL_FROM_KEY}
                        label="SMTP From"
                        name={JAVAMAIL_FROM_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_FROM_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_FROM_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_FROM_KEY]}
                    />

                    <CheckboxInput
                        id={JAVAMAIL_AUTH_KEY}
                        label="SMTP Auth"
                        name={JAVAMAIL_AUTH_KEY}
                        isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_KEY]}
                    />

                    <TextInput
                        id={JAVAMAIL_USER_KEY}
                        label="SMTP User"
                        name={JAVAMAIL_USER_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_USER_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_USER_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_USER_KEY]}
                    />

                    <PasswordInput
                        id={JAVAMAIL_PASSWORD_KEY}
                        label="SMTP Password"
                        name={JAVAMAIL_PASSWORD_KEY}
                        value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PASSWORD_KEY)}
                        isSet={FieldModelUtil.isFieldModelValueSet(fieldModel, JAVAMAIL_PASSWORD_KEY)}
                        onChange={this.handleChange}
                        errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PASSWORD_KEY)}
                        errorValue={this.props.fieldErrors[JAVAMAIL_PASSWORD_KEY]}
                    />
                    <CollapsiblePane title="Advanced Settings">
                        <NumberInput
                            id={JAVAMAIL_PORT_KEY}
                            label="SMTP Port"
                            name={JAVAMAIL_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PORT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_CONNECTION_TIMEOUT_KEY}
                            label="SMTP Connection Timeout"
                            name={JAVAMAIL_CONNECTION_TIMEOUT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_CONNECTION_TIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_CONNECTION_TIMEOUT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_TIMEOUT_KEY}
                            label="SMTP Timeout"
                            name={JAVAMAIL_TIMEOUT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_TIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_TIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_TIMEOUT_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_WRITETIMEOUT_KEY}
                            label="SMTP Write Timeout"
                            name={JAVAMAIL_WRITETIMEOUT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_WRITETIMEOUT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_WRITETIMEOUT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_WRITETIMEOUT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_LOCALHOST_KEY}
                            label="SMTP Localhost"
                            name={JAVAMAIL_LOCALHOST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_LOCALHOST_ADDRESS_KEY}
                            label="SMTP Local Address"
                            name={JAVAMAIL_LOCALHOST_ADDRESS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_ADDRESS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_ADDRESS_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_LOCALHOST_PORT_KEY}
                            label="SMTP Local Port"
                            name={JAVAMAIL_LOCALHOST_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_LOCALHOST_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_LOCALHOST_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_LOCALHOST_PORT_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_EHLO_KEY}
                            label="SMTP Ehlo"
                            name={JAVAMAIL_EHLO_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_EHLO_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_EHLO_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_EHLO_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_AUTH_MECHANISMS_KEY}
                            label="SMTP Auth Mechanisms"
                            name={JAVAMAIL_AUTH_MECHANISMS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_MECHANISMS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_MECHANISMS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_LOGIN_DISABLE_KEY}
                            label="SMTP Auth Login Disable"
                            name={JAVAMAIL_AUTH_LOGIN_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_LOGIN_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_LOGIN_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY}
                            label="SMTP Auth Plain Disable"
                            name={JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_LOGIN_PLAIN_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY}
                            label="SMTP Auth Digest MD5 Disable"
                            name={JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_DIGEST_MD5_DISABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_NTLM_DISABLE_KEY}
                            label="SMTP Auth NTLM Disable"
                            name={JAVAMAIL_AUTH_NTLM_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_DISABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_AUTH_NTLM_DOMAIN_KEY}
                            label="SMTP Auth NTLM Domain"
                            name={JAVAMAIL_AUTH_NTLM_DOMAIN_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_DOMAIN_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_DOMAIN_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_AUTH_NTLM_FLAGS_KEY}
                            label="SMTP Auth NTLM Flags"
                            name={JAVAMAIL_AUTH_NTLM_FLAGS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_NTLM_FLAGS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_NTLM_FLAGS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY}
                            label="SMTP Auth XOAuth2 Disable"
                            name={JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_AUTH_XOAUTH2_DISABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SUBMITTER_KEY}
                            label="SMTP Submitter"
                            name={JAVAMAIL_SUBMITTER_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SUBMITTER_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SUBMITTER_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SUBMITTER_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_DSN_NOTIFY_KEY}
                            label="SMTP DNS Notify"
                            name={JAVAMAIL_DSN_NOTIFY_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_DSN_NOTIFY_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_DSN_NOTIFY_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_DSN_NOTIFY_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_DSN_RET_KEY}
                            label="SMTP DNS Ret"
                            name={JAVAMAIL_DSN_RET_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_DSN_RET_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_DSN_RET_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_DSN_RET_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_ALLOW_8_BITMIME_KEY}
                            label="SMTP Allow 8-bit Mime"
                            name={JAVAMAIL_ALLOW_8_BITMIME_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_ALLOW_8_BITMIME_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_ALLOW_8_BITMIME_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SEND_PARTIAL_KEY}
                            label="SMTP Send Partial"
                            name={JAVAMAIL_SEND_PARTIAL_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SEND_PARTIAL_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SEND_PARTIAL_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SEND_PARTIAL_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SASL_ENABLE_KEY}
                            label="SMTP SASL Enable"
                            name={JAVAMAIL_SASL_ENABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SASL_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_ENABLE_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_MECHANISMS_KEY}
                            label="SMTP SASL Mechanisms"
                            name={JAVAMAIL_SASL_MECHANISMS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_MECHANISMS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_MECHANISMS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_MECHANISMS_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_AUTHORIZATION_ID_KEY}
                            label="SMTP SASL Authorization ID"
                            name={JAVAMAIL_SASL_AUTHORIZATION_ID_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_AUTHORIZATION_ID_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_AUTHORIZATION_ID_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SASL_REALM_KEY}
                            label="SMTP SASL Realm"
                            name={JAVAMAIL_SASL_REALM_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SASL_REALM_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_REALM_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_REALM_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY}
                            label="SMTP SASL Use Canonical Hostname"
                            name={JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SASL_USE_CANONICAL_HOSTNAME_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_QUITWAIT_KEY}
                            label="SMTP QuitWait"
                            name={JAVAMAIL_QUITWAIT_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_QUITWAIT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_QUITWAIT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_QUITWAIT_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_REPORT_SUCCESS_KEY}
                            label="SMTP Report Success"
                            name={JAVAMAIL_REPORT_SUCCESS_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_REPORT_SUCCESS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_REPORT_SUCCESS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_REPORT_SUCCESS_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SSL_ENABLE_KEY}
                            label="SMTP SSL Enable"
                            name={JAVAMAIL_SSL_ENABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SSL_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_ENABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY}
                            label="SMTP SSL Check Server Identity"
                            name={JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_CHECKSERVERIDENTITY_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_TRUST_KEY}
                            label="SMTP SSL Trust"
                            name={JAVAMAIL_SSL_TRUST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_TRUST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_TRUST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_TRUST_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_PROTOCOLS_KEY}
                            label="SMTP SSL Protocols"
                            name={JAVAMAIL_SSL_PROTOCOLS_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_PROTOCOLS_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_PROTOCOLS_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SSL_CIPHERSUITES_KEY}
                            label="SMTP SSL Cipher Suites"
                            name={JAVAMAIL_SSL_CIPHERSUITES_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SSL_CIPHERSUITES_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SSL_CIPHERSUITES_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_STARTTLS_ENABLE_KEY}
                            label="SMTP Start TLS Enabled"
                            name={JAVAMAIL_STARTTLS_ENABLE_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_STARTTLS_ENABLE_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_STARTTLS_ENABLE_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_STARTTLS_REQUIRED_KEY}
                            label="SMTP Start TLS Required"
                            name={JAVAMAIL_STARTTLS_REQUIRED_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_STARTTLS_REQUIRED_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_STARTTLS_REQUIRED_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_PROXY_HOST_KEY}
                            label="SMTP Proxy Host"
                            name={JAVAMAIL_PROXY_HOST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PROXY_HOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PROXY_HOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PROXY_HOST_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_PROXY_PORT_KEY}
                            label="SMTP Proxy Port"
                            name={JAVAMAIL_PROXY_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_PROXY_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_PROXY_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_PROXY_PORT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_SOCKS_HOST_KEY}
                            label="SMTP Socks Host"
                            name={JAVAMAIL_SOCKS_HOST_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SOCKS_HOST_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SOCKS_HOST_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SOCKS_HOST_KEY]}
                        />

                        <NumberInput
                            id={JAVAMAIL_SOCKS_PORT_KEY}
                            label="SMTP Socks Port"
                            name={JAVAMAIL_SOCKS_PORT_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_SOCKS_PORT_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_SOCKS_PORT_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_SOCKS_PORT_KEY]}
                        />

                        <TextInput
                            id={JAVAMAIL_MAILEXTENSION_KEY}
                            label="SMTP Mail Extension"
                            name={JAVAMAIL_MAILEXTENSION_KEY}
                            value={FieldModelUtil.getFieldModelSingleValue(fieldModel, JAVAMAIL_MAILEXTENSION_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_MAILEXTENSION_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_MAILEXTENSION_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_USERSET_KEY}
                            label="SMTP Use RSET"
                            name={JAVAMAIL_USERSET_KEY}
                            isChecked={FieldModelUtil.getFieldModelBooleanValue(fieldModel, JAVAMAIL_USERSET_KEY)}
                            onChange={this.handleChange}
                            errorName={FieldModelUtil.createFieldModelErrorKey(JAVAMAIL_USERSET_KEY)}
                            errorValue={this.props.fieldErrors[JAVAMAIL_USERSET_KEY]}
                        />

                        <CheckboxInput
                            id={JAVAMAIL_NOOP_STRICT_KEY}
                            label="SMTP NoOp Strict"
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
