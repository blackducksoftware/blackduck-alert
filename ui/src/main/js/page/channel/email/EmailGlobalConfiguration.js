import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import {
    EMAIL_GLOBAL_ADVANCED_FIELD_KEYS, EMAIL_GLOBAL_FIELD_KEYS, EMAIL_INFO, EMAIL_TEST_FIELD
} from 'page/channel/email/EmailModels';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import TextInput from 'common/input/TextInput';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import CheckboxInput from 'common/input/CheckboxInput';
import CollapsiblePane from 'common/CollapsiblePane';
import NumberInput from 'common/input/NumberInput';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';
import PasswordInput from 'common/input/PasswordInput';

const EmailGlobalConfiguration = ({ csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete }) => {
    const [fieldModel, setFieldModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, EMAIL_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [testFieldData, setTestFieldData] = useState({});

    const testField = (
        <TextInput
            id={EMAIL_TEST_FIELD.key}
            name={EMAIL_TEST_FIELD.key}
            label={EMAIL_TEST_FIELD.label}
            description={EMAIL_TEST_FIELD.description}
            onChange={FieldModelUtilities.handleTestChange(testFieldData, setTestFieldData)}
            value={testFieldData[EMAIL_TEST_FIELD.key]}
        />
    );

    const hasAdvancedConfig = Object.keys(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS).some((key) => FieldModelUtilities.hasValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS[key]));

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(EMAIL_INFO.key, csrfToken);
        if (data) {
            setFieldModel(data);
        }
    };

    return (
        <CommonGlobalConfiguration
            label={EMAIL_INFO.label}
            description="Configure the email server that Alert will send emails to."
            lastUpdated={fieldModel.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                csrfToken={csrfToken}
                formData={fieldModel}
                setErrors={(errors) => setErrors(errors)}
                setFormData={(content) => setFieldModel(content)}
                testFields={testField}
                testFormData={testFieldData}
                setTestFormData={(values) => setTestFieldData(values)}
                buttonIdPrefix={EMAIL_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={displayDelete}
                errorHandler={errorHandler}
            >
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.host}
                    name={EMAIL_GLOBAL_FIELD_KEYS.host}
                    label="SMTP Host"
                    description="The host name of the SMTP email server."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.host)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.host)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.host]}
                />
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.from}
                    name={EMAIL_GLOBAL_FIELD_KEYS.from}
                    label="SMTP From"
                    description="The email address to use as the return address."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.from)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.from)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.from]}
                />
                <CheckboxInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.auth}
                    name={EMAIL_GLOBAL_FIELD_KEYS.auth}
                    label="SMTP Auth"
                    description="Select this if your SMTP server requires authentication, then fill in the SMTP User and SMTP Password."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.auth)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.auth)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.auth]}
                />
                <TextInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.user}
                    name={EMAIL_GLOBAL_FIELD_KEYS.user}
                    label="SMTP User"
                    description="The username to authenticate with the SMTP server."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.user)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.user)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.user]}
                />
                <PasswordInput
                    id={EMAIL_GLOBAL_FIELD_KEYS.password}
                    name={EMAIL_GLOBAL_FIELD_KEYS.password}
                    label="SMTP Password"
                    description="The password to authenticate with the SMTP server."
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                    value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.password)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(fieldModel, EMAIL_GLOBAL_FIELD_KEYS.password)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_FIELD_KEYS.password)}
                    errorValue={errors.fieldErrors[EMAIL_GLOBAL_FIELD_KEYS.password]}
                />

                <CollapsiblePane
                    id="email_advanced"
                    title="Advanced"
                    expanded={hasAdvancedConfig}
                >
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port}
                        label="SMTP Port"
                        description="The SMTP server port to connect to."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.port]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.connectionTimeout}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.connectionTimeout}
                        label="SMTP Connection Timeout"
                        description="Socket connection timeout value in milliseconds."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.connectionTimeout)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.connectionTimeout)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.connectionTimeout]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.timeout}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.timeout}
                        label="SMTP Timeout"
                        description="Socket read timeout value in milliseconds."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.timeout)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.timeout)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.timeout]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.writeTimeout}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.writeTimeout}
                        label="SMTP Write Timeout"
                        description="Socket write timeout value in milliseconds."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.writeTimeout)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.writeTimeout)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.writeTimeout]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localhost}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localhost}
                        label="SMTP Localhost"
                        description="Local host name used in the SMTP HELO or EHLO command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localhost)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localhost)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localhost]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localAddress}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localAddress}
                        label="SMTP Local Address"
                        description="Local address (host name) to bind to when creating the SMTP socket."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localAddress)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localAddress)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localAddress]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localPort}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localPort}
                        label="SMTP Local Port"
                        description="Local port number to bind to when creating the SMTP socket."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localPort)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localPort)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.localPort]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.ehlo}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.ehlo}
                        label="SMTP Ehlo"
                        description="If false, do not attempt to sign on with the EHLO command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.ehlo)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.ehlo)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.ehlo]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authMechanisms}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authMechanisms}
                        label="SMTP Auth Mechanisms"
                        description="If set, lists the authentication mechanisms to consider, and the order in which to consider them. Only mechanisms supported by the server and supported by the current implementation will be used."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authMechanisms)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authMechanisms)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authMechanisms]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.loginDisable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.loginDisable}
                        label="SMTP Auth Login Disable"
                        description="If true, prevents use of the AUTH LOGIN command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.loginDisable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.loginDisable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.loginDisable]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authPlainDisable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authPlainDisable}
                        label="SMTP Auth Plain Disable"
                        description="If true, prevents use of the AUTH PLAIN command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authPlainDisable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authPlainDisable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authPlainDisable]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authDigestMd5Disable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authDigestMd5Disable}
                        label="SMTP Auth Digest MD5 Disable"
                        description="If true, prevents use of the AUTH DIGEST-MD5 command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authDigestMd5Disable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authDigestMd5Disable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authDigestMd5Disable]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDisable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDisable}
                        label="SMTP Auth Ntlm Disable"
                        description="If true, prevents use of the AUTH NTLM command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDisable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDisable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDisable]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDomain}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDomain}
                        label="SMTP Auth Ntlm Domain"
                        description="The NTLM authentication domain."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDomain)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDomain)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmDomain]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmFlags}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmFlags}
                        label="SMTP Auth Ntlm Flags"
                        description="NTLM protocol-specific flags."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmFlags)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmFlags)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authNtlmFlags]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authXoauth2Disable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authXoauth2Disable}
                        label="SMTP Auth XOAuth2 Disable"
                        description="If true, prevents use of the AUTHENTICATE XOAUTH2 command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authXoauth2Disable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authXoauth2Disable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.authXoauth2Disable]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.submitter}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.submitter}
                        label="SMTP Submitter"
                        description="The submitter to use in the AUTH tag in the MAIL FROM command. Typically used by a mail relay to pass along information about the original submitter of the message."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.submitter)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.submitter)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.submitter]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnNotify}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnNotify}
                        label="SMTP DSN Notify"
                        description="The NOTIFY option to the RCPT command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnNotify)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnNotify)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnNotify]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnRet}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnRet}
                        label="SMTP DSN Ret"
                        description="The RET option to the MAIL command."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnRet)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnRet)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.dsnRet]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.allow8BitMime}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.allow8BitMime}
                        label="SMTP Allow 8-bit Mime"
                        description='If set to true, and the server supports the 8BITMIME extension, text parts of messages that use the "quoted-printable" or "base64" encodings are converted to use "8bit" encoding.'
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.allow8BitMime)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.allow8BitMime)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.allow8BitMime]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sendPartial}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sendPartial}
                        label="SMTP Send Partial"
                        description="If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sendPartial)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sendPartial)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sendPartial]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslEnable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslEnable}
                        label="SMTP SASL Enable"
                        description="If set to true, attempt to use the javax.security.sasl package to choose an authentication mechanism for login."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslEnable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslEnable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslEnable]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslMechanism}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslMechanism}
                        label="SMTP SASL Mechanisms"
                        description="A space or comma separated list of SASL mechanism names to try to use."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslMechanism)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslMechanism)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslMechanism]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslAuthorizationId}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslAuthorizationId}
                        label="SMTP SASL Authorization ID"
                        description="The authorization ID to use in the SASL authentication. If not set, the authentication ID (user name) is used."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslAuthorizationId)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslAuthorizationId)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslAuthorizationId]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslRealm}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslRealm}
                        label="SMTP SASL Realm"
                        description="The realm to use with DIGEST-MD5 authentication."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslRealm)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslRealm)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslRealm]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslUseCanonicalHostname}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslUseCanonicalHostname}
                        label="SMTP SASL Use Canonical Hostname"
                        description="If set to true, the canonical host name returned by InetAddress.getCanonicalHostName is passed to the SASL mechanism, instead of the host name used to connect."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslUseCanonicalHostname)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslUseCanonicalHostname)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.saslUseCanonicalHostname]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.quitWait}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.quitWait}
                        label="SMTP Quit Wait"
                        description="If set to false, the QUIT command is sent and the connection is immediately closed."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.quitWait)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.quitWait)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.quitWait]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.reportSuccess}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.reportSuccess}
                        label="SMTP Report Success"
                        description="If set to true, causes the transport to include an SMTPAddressSucceededException for each address that is successful."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.reportSuccess)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.reportSuccess)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.reportSuccess]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslEnable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslEnable}
                        label="SMTP SSL Enable"
                        description="If set to true, use SSL to connect and use the SSL port by default."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslEnable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslEnable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslEnable]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCheckServerIdentity}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCheckServerIdentity}
                        label="SMTP SSL Check Server Identity"
                        description="If set to true, check the server identity as specified by RFC 2595."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCheckServerIdentity)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCheckServerIdentity)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCheckServerIdentity]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslTrust}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslTrust}
                        label="SMTP SSL Trust"
                        description='If set, and a socket factory hasnt been specified, enables use of a MailSSLSocketFactory. If set to "*", all hosts are trusted.'
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslTrust)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslTrust)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslTrust]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslProtocols}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslProtocols}
                        label="SMTP SSL Protocols"
                        description="Specifies the SSL protocols that will be enabled for SSL connections."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslProtocols)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslProtocols)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslProtocols]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCipherSuites}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCipherSuites}
                        label="SMTP SSL Cipher Suites"
                        description="Specifies the SSL cipher suites that will be enabled for SSL connections."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCipherSuites)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCipherSuites)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.sslCipherSuites]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsEnable}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsEnable}
                        label="SMTP Start TLS Enabled"
                        description="If true, enables the use of the STARTTLS command (if supported by the server) to switch the connection to a TLS-protected connection before issuing any login commands. If the server does not support STARTTLS, the connection continues without the use of TLS."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsEnable)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsEnable)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsEnable]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsRequired}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsRequired}
                        label="SMTP Start TLS Required"
                        description="If true, requires the use of the STARTTLS command. If the server doesnt support the STARTTLS command, or the command fails, the connect method will fail."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsRequired)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsRequired)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.startTlsRequired]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyHost}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyHost}
                        label="SMTP Proxy Host"
                        description="Specifies the host name of an HTTP web proxy server that will be used for connections to the mail server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyHost)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyHost)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyHost]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyPort}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyPort}
                        label="SMTP Proxy Port"
                        description="Specifies the port number for the HTTP web proxy server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyPort)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyPort)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.proxyPort]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksHost}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksHost}
                        label="SMTP Socks Host"
                        description="Specifies the host name of a SOCKS5 proxy server that will be used for connections to the mail server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksHost)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksHost)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksHost]}
                    />
                    <NumberInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksPort}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksPort}
                        label="SMTP Socks Port"
                        description="Specifies the port number for the SOCKS5 proxy server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksPort)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksPort)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.socksPort]}
                    />
                    <TextInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.mailExtensions}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.mailExtensions}
                        label="SMTP Mail Extension"
                        description="Extension string to append to the MAIL command. The extension string can be used to specify standard SMTP service extensions as well as vendor-specific extensions."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        value={FieldModelUtilities.getFieldModelSingleValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.mailExtensions)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.mailExtensions)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.mailExtensions]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.userSet}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.userSet}
                        label="SMTP Use RSET"
                        description="If set to true, use the RSET command instead of the NOOP command in the isConnected method."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.userSet)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.userSet)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.userSet]}
                    />
                    <CheckboxInput
                        id={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.noopStrict}
                        name={EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.noopStrict}
                        label="SMTP NoOp Strict"
                        description="If set to true, insist on a 250 response code from the NOOP command to indicate success."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(fieldModel, setFieldModel)}
                        isChecked={FieldModelUtilities.getFieldModelBooleanValue(fieldModel, EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.noopStrict)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.noopStrict)}
                        errorValue={errors.fieldErrors[EMAIL_GLOBAL_ADVANCED_FIELD_KEYS.noopStrict]}
                    />
                </CollapsiblePane>
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

EmailGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

EmailGlobalConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default EmailGlobalConfiguration;
