import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import * as fieldModelUtilities from 'common/util/fieldModelUtilities';
import PasswordInput from 'common/component/input/PasswordInput';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { SETTINGS_FIELD_KEYS, SETTINGS_INFO } from 'page/settings/SettingsModel';
import ReadOnlyField from 'common/component/input/field/ReadOnlyField';
import FormCard from 'common/component/FormCard';

const SettingsEncryptionConfiguration = ({
    csrfToken, errorHandler, readonly, displaySave
}) => {
    const encryptionRequestUrl = `${ConfigurationRequestBuilder.ENCRYPTION_API_URL}`;

    const [settingsEncryptionConfig, setSettingsEncryptionConfig] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [encryptionReadOnly, setEncryptionReadOnly] = useState(readonly);

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(encryptionRequestUrl, csrfToken);
        const data = await response.json();

        if (response.ok && data) {
            setEncryptionReadOnly(readonly || data.readOnly);
            setSettingsEncryptionConfig(data);
        } else {
            setEncryptionReadOnly(readonly);
            setSettingsEncryptionConfig({});
        }
    };

    const configSourceValue = encryptionReadOnly ? 'Encryption values were set during installation. Please contact your system administrator to change.' : '';

    return (
        <FormCard formTitle="Encryption Configuration">
            <ConcreteConfigurationForm
                csrfToken={csrfToken}
                setErrors={(formErrors) => setErrors(formErrors)}
                buttonIdPrefix={SETTINGS_INFO.key}
                getRequest={fetchData}
                deleteRequest={() => null}
                createRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(encryptionRequestUrl, csrfToken, settingsEncryptionConfig, settingsEncryptionConfig)}
                updateRequest={() => ConfigurationRequestBuilder.createUpdateWithoutIdRequest(encryptionRequestUrl, csrfToken, settingsEncryptionConfig, settingsEncryptionConfig)}
                validateRequest={() => ConfigurationRequestBuilder.createValidateRequest(encryptionRequestUrl, csrfToken, settingsEncryptionConfig)}
                readonly={encryptionReadOnly}
                displaySave={displaySave}
                displayTest={false}
                displayDelete={false}
                errorHandler={errorHandler}
            >
                <ReadOnlyField
                    label=""
                    description={null}
                    // This field is only used to display the source of truth for encryption values when they are read only,
                    // so it doesn't need an id or name. It should be a message box at the top
                    value={configSourceValue}
                />
                <PasswordInput
                    id={SETTINGS_FIELD_KEYS.encryptionPassword}
                    name="encryptionPassword"
                    label="Encryption Password"
                    fieldDescription="The password used when encrypting sensitive fields. Must be at least 8 characters long."
                    required
                    readOnly={encryptionReadOnly}
                    onChange={fieldModelUtilities.handleTestChange(settingsEncryptionConfig, setSettingsEncryptionConfig)}
                    value={settingsEncryptionConfig.encryptionPassword || undefined}
                    isSet={settingsEncryptionConfig.isEncryptionPasswordSet}
                    errorName="encryptionPassword"
                    errorValue={errors.fieldErrors.encryptionPassword}
                />
                <PasswordInput
                    id={SETTINGS_FIELD_KEYS.encryptionGlobalSalt}
                    name="encryptionGlobalSalt"
                    label="Encryption Global Salt"
                    fieldDescription="The salt used when encrypting sensitive fields. Must be at least 8 characters long."
                    required
                    readOnly={encryptionReadOnly}
                    onChange={fieldModelUtilities.handleTestChange(settingsEncryptionConfig, setSettingsEncryptionConfig)}
                    value={settingsEncryptionConfig.encryptionGlobalSalt || undefined}
                    isSet={settingsEncryptionConfig.isEncryptionGlobalSaltSet}
                    errorName="encryptionGlobalSalt"
                    errorValue={errors.fieldErrors.encryptionGlobalSalt}
                />
            </ConcreteConfigurationForm>
        </FormCard>
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
