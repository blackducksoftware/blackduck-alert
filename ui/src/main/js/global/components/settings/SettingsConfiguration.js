import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import CommonGlobalConfigurationForm from 'global/CommonGlobalConfigurationForm';
import { SETTINGS_FIELD_KEYS, SETTINGS_INFO } from 'global/components/settings/SettingsModel';
import PasswordInput from 'common/field/input/PasswordInput';
import CollapsiblePane from 'common/CollapsiblePane';
import TextInput from 'common/field/input/TextInput';
import NumberInput from 'common/field/input/NumberInput';
import * as GlobalRequestHelper from 'global/GlobalRequestHelper';

const SettingsConfiguration = ({ csrfToken, readonly }) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, SETTINGS_INFO.key));
    const [errors, setErrors] = useState({});

    const shouldExpand = FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyHost)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyPort)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyPassword)
        || FieldModelUtilities.hasValue(formData, SETTINGS_FIELD_KEYS.proxyUsername);

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(SETTINGS_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

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
                    errorValue={errors[SETTINGS_FIELD_KEYS.encryptionPassword]}
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
                    errorValue={errors[SETTINGS_FIELD_KEYS.encryptionGlobalSalt]}
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
                        errorValue={errors[SETTINGS_FIELD_KEYS.proxyHost]}
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
                        errorValue={errors[SETTINGS_FIELD_KEYS.proxyPort]}
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
                        errorValue={errors[SETTINGS_FIELD_KEYS.proxyUsername]}
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
                        errorValue={errors[SETTINGS_FIELD_KEYS.proxyPassword]}
                    />
                </CollapsiblePane>
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

SettingsConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

SettingsConfiguration.defaultProps = {
    readonly: false
};

export default SettingsConfiguration;
