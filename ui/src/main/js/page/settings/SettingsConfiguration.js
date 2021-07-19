import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import { SETTINGS_FIELD_KEYS, SETTINGS_INFO } from 'page/settings/SettingsModel';
import PasswordInput from 'common/input/PasswordInput';
import CollapsiblePane from 'common/CollapsiblePane';
import TextInput from 'common/input/TextInput';
import NumberInput from 'common/input/NumberInput';
import DynamicSelectInput from 'common/input/DynamicSelectInput';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';

const SettingsConfiguration = ({
    csrfToken, errorHandler, readonly, displaySave
}) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, SETTINGS_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const shouldExpand = FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyHost)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyPort)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyPassword)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyUsername)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyNonProxyHosts);

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(SETTINGS_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    const nonProxyHostOptions = [
        { label: 'Hosted Azure Boards OAuth (app.vssps.visualstudio.com)', value: 'app.vssps.visualstudio.com' },
        { label: 'Hosted Azure Boards API (dev.azure.com)', value: 'dev.azure.com' },
        { label: 'Jira Cloud (*.atlassian.net)', value: '*.atlassian.net' },
        { label: 'Hosted MS Teams (*.office.com)', value: '*.office.com' },
        { label: 'Hosted Slack (*.slack.com)', value: '*.slack.com' }
    ];

    return (
        <CommonGlobalConfiguration
            label={SETTINGS_INFO.label}
            description="This page allows you to configure the global settings."
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(error) => setErrors(error)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                displayTest={false}
                displayDelete={false}
                buttonIdPrefix={SETTINGS_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                displaySave={displaySave}
                errorHandler={errorHandler}
            >
                <h2 key="settings-header">Encryption Configuration</h2>
                <PasswordInput
                    id={SETTINGS_FIELD_KEYS.encryptionPassword}
                    name={SETTINGS_FIELD_KEYS.encryptionPassword}
                    label="Encryption Password"
                    description="The password used when encrypting sensitive fields. Must be at least 8 characters long."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, SETTINGS_FIELD_KEYS.encryptionPassword)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, SETTINGS_FIELD_KEYS.encryptionPassword)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.encryptionPassword)}
                    errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.encryptionPassword]}
                />
                <PasswordInput
                    id={SETTINGS_FIELD_KEYS.encryptionGlobalSalt}
                    name={SETTINGS_FIELD_KEYS.encryptionGlobalSalt}
                    label="Encryption Global Salt"
                    description="The salt used when encrypting sensitive fields. Must be at least 8 characters long."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, SETTINGS_FIELD_KEYS.encryptionGlobalSalt)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, SETTINGS_FIELD_KEYS.encryptionGlobalSalt)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.encryptionGlobalSalt)}
                    errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.encryptionGlobalSalt]}
                />
                <CollapsiblePane title="Proxy Configuration" expanded={shouldExpand}>
                    <TextInput
                        id={SETTINGS_FIELD_KEYS.proxyHost}
                        name={SETTINGS_FIELD_KEYS.proxyHost}
                        label="Proxy Host"
                        description="The host name of the proxy server to use."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SETTINGS_FIELD_KEYS.proxyHost)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.proxyHost)}
                        errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.proxyHost]}
                    />
                    <NumberInput
                        id={SETTINGS_FIELD_KEYS.proxyPort}
                        name={SETTINGS_FIELD_KEYS.proxyPort}
                        label="Proxy Port"
                        description="The port of the proxy server to use."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SETTINGS_FIELD_KEYS.proxyPort)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.proxyPort)}
                        errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.proxyPort]}
                    />
                    <TextInput
                        id={SETTINGS_FIELD_KEYS.proxyUsername}
                        name={SETTINGS_FIELD_KEYS.proxyUsername}
                        label="Proxy Username"
                        description="If the proxy server requires authentication, the username to authenticate with the proxy server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SETTINGS_FIELD_KEYS.proxyUsername)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.proxyUsername)}
                        errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.proxyUsername]}
                    />
                    <PasswordInput
                        id={SETTINGS_FIELD_KEYS.proxyPassword}
                        name={SETTINGS_FIELD_KEYS.proxyPassword}
                        label="Proxy Password"
                        description="If the proxy server requires authentication, the password to authenticate with the proxy server."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelSingleValue(formData, SETTINGS_FIELD_KEYS.proxyPassword)}
                        isSet={FieldModelUtilities.isFieldModelValueSet(formData, SETTINGS_FIELD_KEYS.proxyPassword)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.proxyPassword)}
                        errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.proxyPassword]}
                    />
                    <DynamicSelectInput
                        id={SETTINGS_FIELD_KEYS.proxyNonProxyHosts}
                        name={SETTINGS_FIELD_KEYS.proxyNonProxyHosts}
                        label="Non-Proxy Hosts"
                        description="Hosts whose network traffic should not go through the proxy."
                        readOnly={readonly}
                        onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                        value={FieldModelUtilities.getFieldModelValues(formData, SETTINGS_FIELD_KEYS.proxyNonProxyHosts)}
                        errorName={FieldModelUtilities.createFieldModelErrorKey(SETTINGS_FIELD_KEYS.proxyNonProxyHosts)}
                        errorValue={errors.fieldErrors[SETTINGS_FIELD_KEYS.proxyNonProxyHosts]}
                        creatable
                        searchable
                        multiSelect
                        options={nonProxyHostOptions}
                        placeholder="Choose an option or type to add your own"
                    />
                </CollapsiblePane>
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

SettingsConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displaySave: PropTypes.bool
};

SettingsConfiguration.defaultProps = {
    readonly: false,
    displaySave: true
};

export default SettingsConfiguration;
