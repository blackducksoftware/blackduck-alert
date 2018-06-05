import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import CheckboxInput from '../../field/input/CheckboxInput';
import NumberInput from '../../field/input/NumberInput';
import PasswordInput from '../../field/input/PasswordInput';
import TextInput from '../../field/input/TextInput';
import ConfigButtons from '../common/ConfigButtons';

import { getEmailConfig, updateEmailConfig, toggleAdvancedEmailOptions } from '../../store/actions/emailConfig';

class EmailConfiguration extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            errors: []
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.props.getEmailConfig();
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.updateStatus === 'FETCHED' || nextProps.updateStatus === 'UPDATED') {
            this.setState({
                mailSmtpHost: nextProps.mailSmtpHost || '',
                mailSmtpUser: nextProps.mailSmtpUser || '',
                mailSmtpPassword: nextProps.mailSmtpPassword || '',
                mailSmtpPasswordIsSet: nextProps.mailSmtpPasswordIsSet,
                mailSmtpPort: nextProps.mailSmtpPort,
                mailSmtpConnectionTimeout: nextProps.mailSmtpConnectionTimeout,
                mailSmtpTimeout: nextProps.mailSmtpTimeout,
                mailSmtpWriteTimeout: nextProps.mailSmtpWriteTimeout,
                mailSmtpFrom: nextProps.mailSmtpFrom || '',
                mailSmtpLocalhost: nextProps.mailSmtpLocalhost || '',
                mailSmtpLocalAddress: nextProps.mailSmtpLocalAddress || '',
                mailSmtpLocalPort: nextProps.mailSmtpLocalPort,
                mailSmtpEhlo: nextProps.mailSmtpEhlo,
                mailSmtpAuth: nextProps.mailSmtpAuth,
                mailSmtpAuthMechanisms: nextProps.mailSmtpAuthMechanisms || '',
                mailSmtpAuthLoginDisable: nextProps.mailSmtpAuthLoginDisable,
                mailSmtpAuthPlainDisable: nextProps.mailSmtpAuthPlainDisable,
                mailSmtpAuthDigestMd5Disable: nextProps.mailSmtpAuthDigestMd5Disable,
                mailSmtpAuthNtlmDisable: nextProps.mailSmtpAuthNtlmDisable,
                mailSmtpAuthNtlmDomain: nextProps.mailSmtpAuthNtlmDomain || '',
                mailSmtpAuthNtlmFlags: nextProps.mailSmtpAuthNtlmFlags,
                mailSmtpAuthXoauth2Disable: nextProps.mailSmtpAuthXoauth2Disable,
                mailSmtpSubmitter: nextProps.mailSmtpSubmitter || '',
                mailSmtpDnsNotify: nextProps.mailSmtpDnsNotify,
                mailSmtpDnsRet: nextProps.mailSmtpDnsRet,
                mailSmtpAllow8bitmime: nextProps.mailSmtpAllow8bitmime,
                mailSmtpSendPartial: nextProps.mailSmtpSendPartial,
                mailSmtpSaslEnable: nextProps.mailSmtpSaslEnable,
                mailSmtpSaslMechanisms: nextProps.mailSmtpSaslMechanisms || '',
                mailSmtpSaslAuthorizationId: nextProps.mailSmtpSaslAuthorizationId || '',
                mailSmtpSaslRealm: nextProps.mailSmtpSaslRealm || '',
                mailSmtpSaslUseCanonicalHostname: nextProps.mailSmtpSaslUseCanonicalHostname,
                mailSmtpQuitwait: nextProps.mailSmtpQuitwait,
                mailSmtpReportSuccess: nextProps.mailSmtpReportSuccess,
                mailSmtpSslEnable: nextProps.mailSmtpSslEnable,
                mailSmtpSslCheckServerIdentity: nextProps.mailSmtpSslCheckServerIdentity,
                mailSmtpSslTrust: nextProps.mailSmtpSslTrust || '',
                mailSmtpSslProtocols: nextProps.mailSmtpSslProtocols || '',
                mailSmtpSslCipherSuites: nextProps.mailSmtpSslCipherSuites || '',
                mailSmtpStartTlsEnable: nextProps.mailSmtpStartTlsEnable,
                mailSmtpStartTlsRequired: nextProps.mailSmtpStartTlsRequired,
                mailSmtpProxyHost: nextProps.mailSmtpProxyHost || '',
                mailSmtpProxyPort: nextProps.mailSmtpProxyPort,
                mailSmtpSocksHost: nextProps.mailSmtpSocksHost || '',
                mailSmtpSocksPort: nextProps.mailSmtpSocksPort,
                mailSmtpMailExtension: nextProps.mailSmtpMailExtension || '',
                mailSmtpUserSet: nextProps.mailSmtpUserSet,
                mailSmtpNoopStrict: nextProps.mailSmtpNoopStrict
            });
        }
    }

    handleChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        this.setState({
            [target.name]: value
        });
    }

    handleSubmit(evt) {
        evt.preventDefault();
        evt.stopPropagation();
        const { id } = this.props;
        this.props.updateEmailConfig({ id, ...this.state });
    }

    render() {
        const showAdvanced = this.props.showAdvanced;
        const showAdvancedLabel = (showAdvanced) ? 'Hide Advanced' : 'Show Advanced';
        const { errorMessage, updateStatus } = this.props;
        return (
            <div>
                <h1>
                    <span className="fa fa-envelope" />
                    Email
                </h1>
                <form className="form-horizontal" onSubmit={this.handleSubmit}>
                    { errorMessage && <div className="alert alert-danger">
                        { errorMessage }
                    </div> }

                    { updateStatus === 'UPDATED' && <div className="alert alert-success">
                        { 'Update successful' }
                    </div> }

                    <TextInput
                        id="email-host"
                        label="SMTP Host"
                        name="mailSmtpHost"
                        value={this.state.mailSmtpHost}
                        onChange={this.handleChange}
                        errorName="mailSmtpHostError"
                        errorValue={this.props.fieldErrors.mailSmtpHost}
                    />

                    <TextInput
                        id="email-from"
                        label="SMTP From"
                        name="mailSmtpFrom"
                        value={this.state.mailSmtpFrom}
                        onChange={this.handleChange}
                        errorName="mailSmtpFromError"
                        errorValue={this.props.fieldErrors.mailSmtpFrom}
                    />

                    <CheckboxInput
                        id="email-auth"
                        label="SMTP Auth"
                        name="mailSmtpAuth"
                        value={this.state.mailSmtpAuth}
                        onChange={this.handleChange}
                        errorName="mailSmtpAuthError"
                        errorValue={this.props.fieldErrors.mailSmtpAuth}
                    />

                    <TextInput
                        id="email-user"
                        label="SMTP User"
                        name="mailSmtpUser"
                        value={this.state.mailSmtpUser}
                        onChange={this.handleChange}
                        errorName="mailSmtpUserError"
                        errorValue={this.props.fieldErrors.mailSmtpUser}
                    />

                    <PasswordInput
                        id="email-password"
                        label="SMTP Password"
                        name="mailSmtpPassword"
                        value={this.state.mailSmtpPassword}
                        isSet={this.state.mailSmtpPasswordIsSet}
                        onChange={this.handleChange}
                        errorName="mailSmtpPasswordError"
                        errorValue={this.props.fieldErrors.mailSmtpPassword}
                    />

                    <div className="form-group">
                        <div className="col-sm-8 col-sm-offset-3">
                            <button id="email-advanced" type="button" className="btn-link" onClick={() => { this.props.toggleAdvancedEmailOptions(!showAdvanced); return false; }}>
                                {showAdvancedLabel}
                            </button>
                        </div>
                    </div>

                    {showAdvanced &&
                    <div>
                        <NumberInput
                            id="email-port"
                            label="SMTP Port"
                            name="mailSmtpPort"
                            value={this.state.mailSmtpPort}
                            onChange={this.handleChange}
                            errorName="mailSmtpPortError"
                            errorValue={this.props.fieldErrors.mailSmtpPort}
                        />

                        <NumberInput
                            id="email-connectionTimeout"
                            label="SMTP Connection Timeout"
                            name="mailSmtpConnectionTimeout"
                            value={this.state.mailSmtpConnectionTimeout}
                            onChange={this.handleChange}
                            errorName="mailSmtpConnectionTimeoutError"
                            errorValue={this.props.fieldErrors.mailSmtpConnectionTimeout}
                        />

                        <NumberInput
                            id="email-timeout"
                            label="SMTP Timeout"
                            name="mailSmtpTimeout"
                            value={this.state.mailSmtpTimeout}
                            onChange={this.handleChange}
                            errorName="mailSmtpTimeoutError"
                            errorValue={this.props.fieldErrors.mailSmtpTimeout}
                        />

                        <NumberInput
                            id="email-writeTimeout"
                            label="SMTP Write Timeout"
                            name="mailSmtpWriteTimeout"
                            value={this.state.mailSmtpWriteTimeout}
                            onChange={this.handleChange}
                            errorName="mailSmtpWriteTimeoutError"
                            errorValue={this.props.fieldErrors.mailSmtpWriteTimeout}
                        />

                        <TextInput
                            id="email-localhost"
                            label="SMTP Localhost"
                            name="mailSmtpLocalhost"
                            value={this.state.mailSmtpLocalhost}
                            onChange={this.handleChange}
                            errorName="mailSmtpLocalhostError"
                            errorValue={this.props.fieldErrors.mailSmtpLocalhost}
                        />

                        <TextInput
                            id="email-localAddress"
                            label="SMTP Local Address"
                            name="mailSmtpLocalAddress"
                            value={this.state.mailSmtpLocalAddress}
                            onChange={this.handleChange}
                            errorName="mailSmtpLocalAddressError"
                            errorValue={this.props.fieldErrors.mailSmtpLocalAddress}
                        />

                        <NumberInput
                            id="email-localPort"
                            label="SMTP Local Port"
                            name="mailSmtpLocalPort"
                            value={this.state.mailSmtpLocalPort}
                            onChange={this.handleChange}
                            errorName="mailSmtpLocalPortError"
                            errorValue={this.props.fieldErrors.mailSmtpLocalPort}
                        />

                        <CheckboxInput
                            id="email-ehlo"
                            label="SMTP Ehlo"
                            name="mailSmtpEhlo"
                            value={this.state.mailSmtpEhlo}
                            onChange={this.handleChange}
                            errorName="mailSmtpEhloError"
                            errorValue={this.props.fieldErrors.mailSmtpEhlo}
                        />

                        <TextInput
                            id="email-authMechanisms"
                            label="SMTP Auth Mechanisms"
                            name="mailSmtpAuthMechanisms"
                            value={this.state.mailSmtpAuthMechanisms}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthMechanismsError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthMechanisms}
                        />

                        <CheckboxInput
                            id="email-loginDisable"
                            label="SMTP Auth Login Disable"
                            name="mailSmtpAuthLoginDisable"
                            value={this.state.mailSmtpAuthLoginDisable}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthLoginDisableError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthLoginDisable}
                        />

                        <CheckboxInput
                            id="email-plainDisable"
                            label="SMTP Auth Plain Disable"
                            name="mailSmtpAuthPlainDisable"
                            value={this.state.mailSmtpAuthPlainDisable}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthPlainDisableError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthPlainDisable}
                        />

                        <CheckboxInput
                            id="email-md5Disable"
                            label="SMTP Auth Digest MD5 Disable"
                            name="mailSmtpAuthDigestMd5Disable"
                            value={this.state.mailSmtpAuthDigestMd5Disable}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthDigestMd5DisableError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthDigestMd5Disable}
                        />

                        <CheckboxInput
                            id="email-ntlmDisable"
                            label="SMTP Auth NTLM Disable"
                            name="mailSmtpAuthNtlmDisable"
                            value={this.state.mailSmtpAuthNtlmDisable}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthNtlmDisableError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthNtlmDisable}
                        />

                        <TextInput
                            id="email-ntlmDomain"
                            label="SMTP Auth NTLM Domain"
                            name="mailSmtpAuthNtlmDomain"
                            value={this.state.mailSmtpAuthNtlmDomain}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthNtlmDomainError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthNtlmDomain}
                        />

                        <NumberInput
                            id="email-ntlmFlags"
                            label="SMTP Auth NTLM Flags"
                            name="mailSmtpAuthNtlmFlags"
                            value={this.state.mailSmtpAuthNtlmFlags}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthNtlmFlagsError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthNtlmFlags}
                        />

                        <CheckboxInput
                            id="email-xoauth2Disable"
                            label="SMTP Auth XOAuth2 Disable"
                            name="mailSmtpAuthXoauth2Disable"
                            value={this.state.mailSmtpAuthXoauth2Disable}
                            onChange={this.handleChange}
                            errorName="mailSmtpAuthXoauth2DisableError"
                            errorValue={this.props.fieldErrors.mailSmtpAuthXoauth2Disable}
                        />

                        <TextInput
                            id="email-submitted"
                            label="SMTP Submitter"
                            name="mailSmtpSubmitter"
                            value={this.state.mailSmtpSubmitter}
                            onChange={this.handleChange}
                            errorName="mailSmtpSubmitterError"
                            errorValue={this.props.fieldErrors.mailSmtpSubmitter}
                        />

                        <TextInput
                            id="email-dnsNotify"
                            label="SMTP DNS Notify"
                            name="mailSmtpDnsNotify"
                            value={this.state.mailSmtpDnsNotify}
                            onChange={this.handleChange}
                            errorName="mailSmtpDnsNotifyError"
                            errorValue={this.props.fieldErrors.mailSmtpDnsNotify}
                        />

                        <TextInput
                            id="email-dnsRet"
                            label="SMTP DNS Ret"
                            name="mailSmtpDnsRet"
                            value={this.state.mailSmtpDnsRet}
                            onChange={this.handleChange}
                            errorName="mailSmtpDnsRetError"
                            errorValue={this.props.fieldErrors.mailSmtpDnsRet}
                        />

                        <CheckboxInput
                            id="email-8bitMime"
                            label="SMTP Allow 8-bit Mime"
                            name="mailSmtpAllow8bitmime"
                            value={this.state.mailSmtpAllow8bitmime}
                            onChange={this.handleChange}
                            errorName="mailSmtpAllow8bitmimeError"
                            errorValue={this.props.fieldErrors.mailSmtpAllow8bitmime}
                        />

                        <CheckboxInput
                            id="email-sendPartial"
                            label="SMTP Send Partial"
                            name="mailSmtpSendPartial"
                            value={this.state.mailSmtpSendPartial}
                            onChange={this.handleChange}
                            errorName="mailSmtpSendPartialError"
                            errorValue={this.props.fieldErrors.mailSmtpSendPartial}
                        />

                        <CheckboxInput
                            id="email-saslEnable"
                            label="SMTP SASL Enable"
                            name="mailSmtpSaslEnable"
                            value={this.state.mailSmtpSaslEnable}
                            onChange={this.handleChange}
                            errorName="mailSmtpSaslEnableError"
                            errorValue={this.props.fieldErrors.mailSmtpSaslEnable}
                        />

                        <TextInput
                            id="email-saslMechanisms"
                            label="SMTP SASL Mechanisms"
                            name="mailSmtpSaslMechanisms"
                            value={this.state.mailSmtpSaslMechanisms}
                            onChange={this.handleChange}
                            errorName="mailSmtpSaslMechanismsError"
                            errorValue={this.props.fieldErrors.mailSmtpSaslMechanisms}
                        />

                        <TextInput
                            id="email-saslAuthorizationId"
                            label="SMTP SASL Authorization ID"
                            name="mailSmtpSaslAuthorizationId"
                            value={this.state.mailSmtpSaslAuthorizationId}
                            onChange={this.handleChange}
                            errorName="mailSmtpSaslAuthorizationIdError"
                            errorValue={this.props.fieldErrors.mailSmtpSaslAuthorizationId}
                        />

                        <TextInput
                            id="email-saslrealm"
                            label="SMTP SASL Realm"
                            name="mailSmtpSaslRealm"
                            value={this.state.mailSmtpSaslRealm}
                            onChange={this.handleChange}
                            errorName="mailSmtpSaslRealmError"
                            errorValue={this.props.fieldErrors.mailSmtpSaslRealm}
                        />

                        <CheckboxInput
                            id="email-canonicalHostname"
                            label="SMTP SASL Use Canonical Hostname"
                            name="mailSmtpSaslUseCanonicalHostname"
                            value={this.state.mailSmtpSaslUseCanonicalHostname}
                            onChange={this.handleChange}
                            errorName="mailSmtpSaslUseCanonicalHostnameError"
                            errorValue={this.props.fieldErrors.mailSmtpSaslUseCanonicalHostname}
                        />

                        <CheckboxInput
                            id="email-quitWait"
                            label="SMTP QuitWait"
                            name="mailSmtpQuitwait"
                            value={this.state.mailSmtpQuitwait}
                            onChange={this.handleChange}
                            errorName="mailSmtpQuitwaitError"
                            errorValue={this.props.fieldErrors.mailSmtpQuitwait}
                        />

                        <CheckboxInput
                            id="email-reportSuccess"
                            label="SMTP Report Success"
                            name="mailSmtpReportSuccess"
                            value={this.state.mailSmtpReportSuccess}
                            onChange={this.handleChange}
                            errorName="mailSmtpReportSuccessError"
                            errorValue={this.props.fieldErrors.mailSmtpReportSuccess}
                        />

                        <CheckboxInput
                            id="email-sslEnable"
                            label="SMTP SSL Enable"
                            name="mailSmtpSslEnable"
                            value={this.state.mailSmtpSslEnable}
                            onChange={this.handleChange}
                            errorName="mailSmtpSslEnableError"
                            errorValue={this.props.fieldErrors.mailSmtpSslEnable}
                        />

                        <CheckboxInput
                            id="email-checkServerIdentity"
                            label="SMTP SSL Check Server Identity"
                            name="mailSmtpSslCheckServerIdentity"
                            value={this.state.mailSmtpSslCheckServerIdentity}
                            onChange={this.handleChange}
                            errorName="mailSmtpSslCheckServerIdentityError"
                            errorValue={this.props.fieldErrors.mailSmtpSslCheckServerIdentity}
                        />

                        <TextInput
                            id="email-sslTrust"
                            label="SMTP SSL Trust"
                            name="mailSmtpSslTrust"
                            value={this.state.mailSmtpSslTrust}
                            onChange={this.handleChange}
                            errorName="mailSmtpSslTrustError"
                            errorValue={this.props.fieldErrors.mailSmtpSslTrust}
                        />

                        <TextInput
                            id="email-sslProtocols"
                            label="SMTP SSL Protocols"
                            name="mailSmtpSslProtocols"
                            value={this.state.mailSmtpSslProtocols}
                            onChange={this.handleChange}
                            errorName="mailSmtpSslProtocolsError"
                            errorValue={this.props.fieldErrors.mailSmtpSslProtocols}
                        />

                        <TextInput
                            id="email-cipherSuites"
                            label="SMTP SSL Cipher Suites"
                            name="mailSmtpSslCipherSuites"
                            value={this.state.mailSmtpSslCipherSuites}
                            onChange={this.handleChange}
                            errorName="mailSmtpSslCipherSuitesError"
                            errorValue={this.props.fieldErrors.mailSmtpSslCipherSuites}
                        />

                        <CheckboxInput
                            id="email-tlsEnabled"
                            label="SMTP Start TLS Enabled"
                            name="mailSmtpStartTlsEnable"
                            value={this.state.mailSmtpStartTlsEnable}
                            onChange={this.handleChange}
                            errorName="mailSmtpStartTlsEnableError"
                            errorValue={this.props.fieldErrors.mailSmtpStartTlsEnable}
                        />

                        <CheckboxInput
                            id="email-tlsRequired"
                            label="SMTP Start TLS Required"
                            name="mailSmtpStartTlsRequired"
                            value={this.state.mailSmtpStartTlsRequired}
                            onChange={this.handleChange}
                            errorName="mailSmtpStartTlsRequiredError"
                            errorValue={this.props.fieldErrors.mailSmtpStartTlsRequired}
                        />

                        <TextInput
                            id="email-proxyHost"
                            label="SMTP Proxy Host"
                            name="mailSmtpProxyHost"
                            value={this.state.mailSmtpProxyHost}
                            onChange={this.handleChange}
                            errorName="mailSmtpProxyHostError"
                            errorValue={this.props.fieldErrors.mailSmtpProxyHost}
                        />

                        <NumberInput
                            id="email-proxyPort"
                            label="SMTP Proxy Port"
                            name="mailSmtpProxyPort"
                            value={this.state.mailSmtpProxyPort}
                            onChange={this.handleChange}
                            errorName="mailSmtpProxyPortError"
                            errorValue={this.props.fieldErrors.mailSmtpProxyPort}
                        />

                        <TextInput
                            id="email-socksHost"
                            label="SMTP Socks Host"
                            name="mailSmtpSocksHost"
                            value={this.state.mailSmtpSocksHost}
                            onChange={this.handleChange}
                            errorName="mailSmtpSocksHostError"
                            errorValue={this.props.fieldErrors.mailSmtpSocksHost}
                        />

                        <NumberInput
                            id="email-socksPort"
                            label="SMTP Socks Port"
                            name="mailSmtpSocksPort"
                            value={this.state.mailSmtpSocksPort}
                            onChange={this.handleChange}
                            errorName="mailSmtpSocksPortError"
                            errorValue={this.props.fieldErrors.mailSmtpSocksPort}
                        />

                        <TextInput
                            id="email-mailExtension"
                            label="SMTP Mail Extension"
                            name="mailSmtpMailExtension"
                            value={this.state.mailSmtpMailExtension}
                            onChange={this.handleChange}
                            errorName="mailSmtpMailExtensionError"
                            errorValue={this.props.fieldErrors.mailSmtpMailExtension}
                        />

                        <CheckboxInput
                            id="email-userSet"
                            label="SMTP User Set"
                            name="mailSmtpUserSet"
                            value={this.state.mailSmtpUserSet}
                            onChange={this.handleChange}
                            errorName="mailSmtpUserSetError"
                            errorValue={this.props.fieldErrors.mailSmtpUserSet}
                        />

                        <CheckboxInput
                            id="email-noOpStrict"
                            label="SMTP NoOp Strict"
                            name="mailSmtpNoopStrict"
                            value={this.state.mailSmtpNoopStrict}
                            onChange={this.handleChange}
                            errorName="mailSmtpNoopStrictError"
                            errorValue={this.props.fieldErrors.mailSmtpNoopStrict}
                        />
                    </div>
                    }
                    <ConfigButtons cancelId="email-cancel" submitId="email-submit" includeSave includeTest={false} />
                </form>
            </div>
        );
    }
}

EmailConfiguration.propTypes = {
    id: PropTypes.string,
    mailSmtpHost: PropTypes.string,
    mailSmtpUser: PropTypes.string,
    mailSmtpPassword: PropTypes.string,
    mailSmtpPasswordIsSet: PropTypes.bool.isRequired,
    mailSmtpPort: PropTypes.string,
    mailSmtpConnectionTimeout: PropTypes.string,
    mailSmtpTimeout: PropTypes.string,
    mailSmtpWriteTimeout: PropTypes.string,
    mailSmtpFrom: PropTypes.string,
    mailSmtpLocalhost: PropTypes.string,
    mailSmtpLocalAddress: PropTypes.string,
    mailSmtpLocalPort: PropTypes.string,
    mailSmtpEhlo: PropTypes.bool,
    mailSmtpAuth: PropTypes.bool,
    mailSmtpAuthMechanisms: PropTypes.string,
    mailSmtpAuthLoginDisable: PropTypes.bool,
    mailSmtpAuthPlainDisable: PropTypes.bool,
    mailSmtpAuthDigestMd5Disable: PropTypes.bool,
    mailSmtpAuthNtlmDisable: PropTypes.bool,
    mailSmtpAuthNtlmDomain: PropTypes.string,
    mailSmtpAuthNtlmFlags: PropTypes.string,
    mailSmtpAuthXoauth2Disable: PropTypes.bool,
    mailSmtpSubmitter: PropTypes.string,
    mailSmtpDnsNotify: PropTypes.bool,
    mailSmtpDnsRet: PropTypes.bool,
    mailSmtpAllow8bitmime: PropTypes.bool,
    mailSmtpSendPartial: PropTypes.bool,
    mailSmtpSaslEnable: PropTypes.bool,
    mailSmtpSaslMechanisms: PropTypes.string,
    mailSmtpSaslAuthorizationId: PropTypes.string,
    mailSmtpSaslRealm: PropTypes.string,
    mailSmtpSaslUseCanonicalHostname: PropTypes.boolean,
    mailSmtpQuitwait: PropTypes.bool,
    mailSmtpReportSuccess: PropTypes.bool,
    mailSmtpSslEnable: PropTypes.bool,
    mailSmtpSslCheckServerIdentity: PropTypes.bool,
    mailSmtpSslTrust: PropTypes.string,
    mailSmtpSslProtocols: PropTypes.string,
    mailSmtpSslCipherSuites: PropTypes.string,
    mailSmtpStartTlsEnable: PropTypes.bool,
    mailSmtpStartTlsRequired: PropTypes.bool,
    mailSmtpProxyHost: PropTypes.string,
    mailSmtpProxyPort: PropTypes.string,
    mailSmtpSocksHost: PropTypes.string,
    mailSmtpSocksPort: PropTypes.string,
    mailSmtpMailExtension: PropTypes.string,
    mailSmtpUserSet: PropTypes.bool,
    mailSmtpNoopStrict: PropTypes.bool,
    showAdvanced: PropTypes.bool.isRequired,
    toggleAdvancedEmailOptions: PropTypes.func.isRequired,
    getEmailConfig: PropTypes.func.isRequired,
    errorMessage: PropTypes.string,
    updateStatus: PropTypes.string,
    fieldErrors: PropTypes.arrayOf(PropTypes.any)
};

EmailConfiguration.defaultProps = {
    mailSmtpAuth: false,
    mailSmtpPasswordIsSet: false,
    mailSmtpEhlo: false,
    mailSmtpAllow8bitmime: false,
    mailSmtpSendPartial: false
};

const mapStateToProps = state => ({
    mailSmtpHost: state.emailConfig.mailSmtpHost,
    mailSmtpUser: state.emailConfig.mailSmtpUser,
    mailSmtpPassword: state.emailConfig.mailSmtpPassword,
    mailSmtpPasswordIsSet: state.emailConfig.mailSmtpPasswordIsSet,
    mailSmtpPort: state.emailConfig.mailSmtpPort,
    mailSmtpConnectionTimeout: state.emailConfig.mailSmtpConnectionTimeout,
    mailSmtpTimeout: state.emailConfig.mailSmtpTimeout,
    mailSmtpWriteTimeout: state.emailConfig.mailSmtpWriteTimeout,
    mailSmtpFrom: state.emailConfig.mailSmtpFrom,
    mailSmtpLocalhost: state.emailConfig.mailSmtpLocalhost,
    mailSmtpLocalAddress: state.emailConfig.mailSmtpLocalAddress,
    mailSmtpLocalPort: state.emailConfig.mailSmtpLocalPort,
    mailSmtpEhlo: state.emailConfig.mailSmtpEhlo,
    mailSmtpAuth: state.emailConfig.mailSmtpAuth,
    mailSmtpAuthMechanisms: state.emailConfig.mailSmtpAuthMechanisms,
    mailSmtpAuthLoginDisable: state.emailConfig.mailSmtpAuthLoginDisable,
    mailSmtpAuthPlainDisable: state.emailConfig.mailSmtpAuthPlainDisable,
    mailSmtpAuthDigestMd5Disable: state.emailConfig.mailSmtpAuthDigestMd5Disable,
    mailSmtpAuthNtlmDisable: state.emailConfig.mailSmtpAuthNtlmDisable,
    mailSmtpAuthNtlmDomain: state.emailConfig.mailSmtpAuthNtlmDomain,
    mailSmtpAuthNtlmFlags: state.emailConfig.mailSmtpAuthNtlmFlags,
    mailSmtpAuthXoauth2Disable: state.emailConfig.mailSmtpAuthXoauth2Disable,
    mailSmtpSubmitter: state.emailConfig.mailSmtpSubmitter,
    mailSmtpDnsNotify: state.emailConfig.mailSmtpDnsNotify,
    mailSmtpDnsRet: state.emailConfig.mailSmtpDnsRet,
    mailSmtpAllow8bitmime: state.emailConfig.mailSmtpAllow8bitmime,
    mailSmtpSendPartial: state.emailConfig.mailSmtpSendPartial,
    mailSmtpSaslEnable: state.emailConfig.mailSmtpSaslEnable,
    mailSmtpSaslMechanisms: state.emailConfig.mailSmtpSaslMechanisms,
    mailSmtpSaslAuthorizationId: state.emailConfig.mailSmtpSaslAuthorizationId,
    mailSmtpSaslRealm: state.emailConfig.mailSmtpSaslRealm,
    mailSmtpSaslUseCanonicalHostname: state.emailConfig.mailSmtpSaslUseCanonicalHostname,
    mailSmtpQuitwait: state.emailConfig.mailSmtpQuitwait,
    mailSmtpReportSuccess: state.emailConfig.mailSmtpReportSuccess,
    mailSmtpSslEnable: state.emailConfig.mailSmtpSslEnable,
    mailSmtpSslCheckServerIdentity: state.emailConfig.mailSmtpSslCheckServerIdentity,
    mailSmtpSslTrust: state.emailConfig.mailSmtpSslTrust,
    mailSmtpSslProtocols: state.emailConfig.mailSmtpSslProtocols,
    mailSmtpSslCipherSuites: state.emailConfig.mailSmtpSslCipherSuites,
    mailSmtpStartTlsEnable: state.emailConfig.mailSmtpStartTlsEnable,
    mailSmtpStartTlsRequired: state.emailConfig.mailSmtpStartTlsRequired,
    mailSmtpProxyHost: state.emailConfig.mailSmtpProxyHost,
    mailSmtpProxyPort: state.emailConfig.mailSmtpProxyPort,
    mailSmtpSocksHost: state.emailConfig.mailSmtpSocksHost,
    mailSmtpSocksPort: state.emailConfig.mailSmtpSocksPort,
    mailSmtpMailExtension: state.emailConfig.mailSmtpMailExtension,
    mailSmtpUserSet: state.emailConfig.mailSmtpUserSet,
    mailSmtpNoopStrict: state.emailConfig.mailSmtpNoopStrict,
    showAdvanced: state.emailConfig.showAdvanced,
    id: state.emailConfig.id,
    errorMessage: state.emailConfig.error.message,
    fieldErrors: state.emailConfig.error.fieldErrors,
    updateStatus: state.emailConfig.updateStatus
});

const mapDispatchToProps = dispatch => ({
    toggleAdvancedEmailOptions: toggle => dispatch(toggleAdvancedEmailOptions(toggle)),
    getEmailConfig: () => dispatch(getEmailConfig()),
    updateEmailConfig: config => dispatch(updateEmailConfig(config))

});

export default connect(mapStateToProps, mapDispatchToProps)(EmailConfiguration);
