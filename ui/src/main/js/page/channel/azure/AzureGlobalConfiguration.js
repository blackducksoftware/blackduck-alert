import React, { useState } from 'react';
import * as PropTypes from 'prop-types';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import CommonGlobalConfigurationForm from 'common/global/CommonGlobalConfigurationForm';
import PasswordInput from 'common/input/PasswordInput';
import { AZURE_GLOBAL_FIELD_KEYS, AZURE_INFO } from 'page/channel/azure/AzureModel';
import OAuthEndpointButtonField from 'common/input/field/OAuthEndpointButtonField';
import * as GlobalRequestHelper from 'common/global/GlobalRequestHelper';
import TextInput from 'common/input/TextInput';

const AzureGlobalConfiguration = ({
    csrfToken, errorHandler, readonly, displayTest, displaySave, displayDelete
}) => {
    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, AZURE_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

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
                displayTest={displayTest}
                displaySave={displaySave}
                displayDelete={displayDelete}
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
                    errorValue={errors.fieldErrors[AZURE_GLOBAL_FIELD_KEYS.organization]}
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
                    errorValue={errors.fieldErrors[AZURE_GLOBAL_FIELD_KEYS.clientId]}
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
                    errorValue={errors.fieldErrors[AZURE_GLOBAL_FIELD_KEYS.clientSecret]}
                />
                <OAuthEndpointButtonField
                    id={AZURE_GLOBAL_FIELD_KEYS.configureOAuth}
                    name={AZURE_GLOBAL_FIELD_KEYS.configureOAuth}
                    buttonLabel="Authenticate"
                    label="Microsoft OAuth"
                    description="This will redirect you to Microsoft's OAuth login. To clear the Oauth request cache, please delete and reconfigure the Azure fields.  Please note you will remain logged in; for security reasons you may want to logout of your Microsoft account after authenticating the application."
                    endpoint="/api/function"
                    csrfToken={csrfToken}
                    currentConfig={formData}
                    fieldKey={AZURE_GLOBAL_FIELD_KEYS.configureOAuth}
                    requiredRelatedFields={[
                        AZURE_GLOBAL_FIELD_KEYS.organization,
                        AZURE_GLOBAL_FIELD_KEYS.clientId,
                        AZURE_GLOBAL_FIELD_KEYS.clientSecret
                    ]}
                    readOnly={readonly || !displayTest}
                    onChange={FieldModelUtilities.handleChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.configureOAuth)}
                    errorValue={errors.fieldErrors[AZURE_GLOBAL_FIELD_KEYS.configureOAuth]}
                />
            </CommonGlobalConfigurationForm>
        </CommonGlobalConfiguration>
    );
};

AzureGlobalConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

AzureGlobalConfiguration.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default AzureGlobalConfiguration;
