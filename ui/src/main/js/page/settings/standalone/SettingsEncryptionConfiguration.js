import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as fieldModelUtilities from 'common/util/fieldModelUtilities';
import PasswordInput from 'common/input/PasswordInput';
import ConfigurationForm from 'page/channel/email/standalone/ConfigurationForm';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { SETTINGS_FIELD_KEYS, SETTINGS_INFO } from 'page/settings/SettingsModel';

const SettingsEncryptionConfiguration = ({
    csrfToken, errorHandler, readonly, displaySave
}) => {
    const encryptionRequestUrl = `${ConfigurationRequestBuilder.ENCRYPTION_API_URL}`;

    const [settingsEncryptionConfig, setSettingsEncryptionConfig] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const [passwordFromApiExists, setPasswordFromApiExists] = useState(false);

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(encryptionRequestUrl, csrfToken);
        const data = await response.json();

        if (data) {
            setSettingsEncryptionConfig(data);
            setPasswordFromApiExists(true);
        } else {
            setSettingsEncryptionConfig({});
            setPasswordFromApiExists(false);
        }
    };

    return (
        <ConfigurationForm
            csrfToken={csrfToken}
            setErrors={(formErrors) => setErrors(formErrors)}
            buttonIdPrefix={SETTINGS_INFO.key}
            getRequest={fetchData}
            deleteRequest={() => null}
            createRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(encryptionRequestUrl, csrfToken, settingsEncryptionConfig, settingsEncryptionConfig)}
            updateRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(encryptionRequestUrl, csrfToken, settingsEncryptionConfig, settingsEncryptionConfig)}
            validateRequest={() => ConfigurationRequestBuilder.createValidateRequest(encryptionRequestUrl, csrfToken, settingsEncryptionConfig)}
            readonly={readonly}
            displaySave={displaySave}
            displayTest={false}
            displayDelete={false}
            errorHandler={errorHandler}
        >
            <h2 key="settings-header">Encryption Configuration</h2>
            <PasswordInput
                id={SETTINGS_FIELD_KEYS.encryptionPassword}
                name="encryptionPassword"
                label="Encryption Password"
                description="The password used when encrypting sensitive fields. Must be at least 8 characters long."
                required
                readOnly={readonly}
                onChange={fieldModelUtilities.handleTestChange(settingsEncryptionConfig, setSettingsEncryptionConfig)}
                value={settingsEncryptionConfig.encryptionPassword || undefined}
                isSet={passwordFromApiExists}
                errorName="encryptionPassword"
                errorValue={errors.fieldErrors.encryptionPassword}
            />
            <PasswordInput
                id={SETTINGS_FIELD_KEYS.encryptionGlobalSalt}
                name="encryptionGlobalSalt"
                label="Encryption Global Salt"
                description="The salt used when encrypting sensitive fields. Must be at least 8 characters long."
                required
                readOnly={readonly}
                onChange={fieldModelUtilities.handleTestChange(settingsEncryptionConfig, setSettingsEncryptionConfig)}
                value={settingsEncryptionConfig.encryptionGlobalSalt || undefined}
                isSet={passwordFromApiExists}
                errorName="encryptionGlobalSalt"
                errorValue={errors.fieldErrors.encryptionGlobalSalt}
            />
        </ConfigurationForm>
    );
};

SettingsEncryptionConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displaySave: PropTypes.bool
};

SettingsEncryptionConfiguration.defaultProps = {
    readonly: false,
    displaySave: true
};

export default SettingsEncryptionConfiguration;
