import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import { CONTEXT_TYPE } from 'util/descriptorUtilities';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import CommonGlobalConfigurationForm from 'global/CommonGlobalConfigurationForm';
import PasswordInput from 'field/input/PasswordInput';
import { AZURE_GLOBAL_FIELD_KEYS, AZURE_INFO } from 'channels/azure/AzureModel';
import OAuthEndpointButtonField from 'field/input/OAuthEndpointButtonField';
import * as GlobalRequestHelper from 'global/GlobalRequestHelper';
import TextInput from 'field/input/TextInput';

const AzureGlobalConfiguration = ({ csrfToken, errorHandler, readonly }) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, AZURE_INFO.key));
    const [errors, setErrors] = useState({});

    const retrieveData = async () => {
        const data = await GlobalRequestHelper.getDataFindFirst(AZURE_INFO.key, csrfToken);
        if (data) {
            setFormData(data);
        }
    };

    return (
        <CommonGlobalConfiguration
            label={AZURE_INFO.label}
            description="Configure the Azure Boards instance that Alert will send issue updates to."
            lastUpdated={formData.lastUpdated}
        >
            <CommonGlobalConfigurationForm
                setErrors={(data) => setErrors(data)}
                formData={formData}
                setFormData={(data) => setFormData(data)}
                csrfToken={csrfToken}
                buttonIdPrefix={AZURE_INFO.key}
                retrieveData={retrieveData}
                readonly={readonly}
                errorHandler={errorHandler}
            >
                <TextInput
                    id={AZURE_GLOBAL_FIELD_KEYS.organization}
                    name={AZURE_GLOBAL_FIELD_KEYS.organization}
                    label="Organization Name"
                    description="The name of the Azure DevOps organization."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, AZURE_GLOBAL_FIELD_KEYS.organization)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.organization)}
                    errorValue={errors[AZURE_GLOBAL_FIELD_KEYS.organization]}
                />
                <PasswordInput
                    id={AZURE_GLOBAL_FIELD_KEYS.clientId}
                    name={AZURE_GLOBAL_FIELD_KEYS.clientId}
                    label="App ID"
                    description="The App ID created for Alert when registering your Azure DevOps Client Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, AZURE_GLOBAL_FIELD_KEYS.clientId)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, AZURE_GLOBAL_FIELD_KEYS.clientId)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.clientId)}
                    errorValue={errors[AZURE_GLOBAL_FIELD_KEYS.clientId]}
                />
                <PasswordInput
                    id={AZURE_GLOBAL_FIELD_KEYS.clientSecret}
                    name={AZURE_GLOBAL_FIELD_KEYS.clientSecret}
                    label="Client Secret"
                    description="The Client secret created for Alert when registering your Azure DevOps Application."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    value={FieldModelUtilities.getFieldModelSingleValue(formData, AZURE_GLOBAL_FIELD_KEYS.clientSecret)}
                    isSet={FieldModelUtilities.isFieldModelValueSet(formData, AZURE_GLOBAL_FIELD_KEYS.clientSecret)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.clientSecret)}
                    errorValue={errors[AZURE_GLOBAL_FIELD_KEYS.clientSecret]}
                />
                <OAuthEndpointButtonField
                    id={AZURE_GLOBAL_FIELD_KEYS.configureOAuth}
                    name={AZURE_GLOBAL_FIELD_KEYS.configureOAuth}
                    buttonLabel="Authenticate"
                    label="Microsoft OAuth"
                    description="This will redirect you to Microsoft's OAuth login.  Please note you will remain logged in; for security reasons you may want to logout of your Microsoft account after authenticating the application."
                    endpoint="/api/function"
                    csrfToken={csrfToken}
                    currentConfig={formData}
                    fieldKey={AZURE_GLOBAL_FIELD_KEYS.configureOAuth}
                    requiredRelatedFields={[
                        AZURE_GLOBAL_FIELD_KEYS.organization,
                        AZURE_GLOBAL_FIELD_KEYS.clientId,
                        AZURE_GLOBAL_FIELD_KEYS.clientSecret
                    ]}
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.configureOAuth)}
                    errorValue={errors[AZURE_GLOBAL_FIELD_KEYS.configureOAuth]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

AzureGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.func.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool

};

AzureGlobalConfiguration.defaultProps = {
    readonly: false
};

export default AzureGlobalConfiguration;
