import React, { useEffect, useState } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import * as PropTypes from 'prop-types';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { AZURE_GLOBAL_FIELD_KEYS, AZURE_INFO, AZURE_URLS } from 'page/channel/azure/AzureModel';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import PageHeader from 'common/component/navigation/PageHeader';
import PasswordInput from 'common/component/input/PasswordInput';
import OAuthEndpointButtonField from 'common/component/input/field/OAuthEndpointButtonField';
import TextInput from 'common/component/input/TextInput';
import * as ConfigurationRequestBuilder from 'common/util/configurationRequestBuilder';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';


const AzureBoardForm = ({ csrfToken, errorHandler, readonly, displayTest }) => {
    const { id } = useParams();
    const history = useHistory();

    const [formData, setFormData] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.GLOBAL, AZURE_INFO.key));
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());

    const azureBoardRequestUrl = `${ConfigurationRequestBuilder.AZURE_BOARD_API_URL}`;

    const fetchData = async () => {
        const response = await ConfigurationRequestBuilder.createReadRequest(azureBoardRequestUrl, csrfToken);
        const data = await response.json();
        if (data) {
            setFormData(data);
            
        }
    };

    useEffect(() => {
        // When editing or copying azure board, both id and models field will be present
        if (formData?.models?.length >= 1 && id) {
            const selectedAzureData = formData.models.find(azureModel => azureModel.id === id);
            setFormData(selectedAzureData);
        }
    }, [formData, id]);

    function postData() {
        return ConfigurationRequestBuilder.createNewConfigurationRequest(azureBoardRequestUrl, csrfToken, formData);
    }

    function updateData() {
        return ConfigurationRequestBuilder.createUpdateRequest(azureBoardRequestUrl, csrfToken, id, formData);
    }

    function deleteData() {
        return ConfigurationRequestBuilder.createDeleteRequest(azureBoardRequestUrl, csrfToken, formData.id);
    }

    function validateData() {
        return ConfigurationRequestBuilder.createValidateRequest(azureBoardRequestUrl, csrfToken, formData);
    }

    function testData() {
        return ConfigurationRequestBuilder.createTestRequest(azureBoardRequestUrl, csrfToken, formData);
    }

    return (
        <div>
            <PageHeader
                title={AZURE_INFO.label}
                description="Configure the Azure Boards instance that Alert will send issue updates to."
                lastUpdated={formData.lastUpdated}
            />
            <ConcreteConfigurationForm
                formDataId={formData.id}
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchData}
                deleteRequest={deleteData}
                updateRequest={updateData}
                createRequest={postData}
                validateRequest={validateData}
                testRequest={testData}
                errorHandler={errorHandler}
                afterSuccessfulSave={() => history.push(AZURE_URLS.mainUrl)}
            >
                <TextInput
                    id={AZURE_GLOBAL_FIELD_KEYS.name}
                    name={AZURE_GLOBAL_FIELD_KEYS.name}
                    label="Name"
                    description="The name of the Azure Board for your identification purposes."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.name || undefined}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.name)}
                    errorValue={errors.fieldErrors[AZURE_GLOBAL_FIELD_KEYS.name]}
                />
                <TextInput
                    id={AZURE_GLOBAL_FIELD_KEYS.organization}
                    name={AZURE_GLOBAL_FIELD_KEYS.organization}
                    label="Organization Name"
                    description="The name of the Azure DevOps organization."
                    required
                    readOnly={readonly}
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.organizationName || undefined}
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
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.appId || undefined}
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
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    value={formData.clientSecret || undefined}
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
                    onChange={FieldModelUtilities.handleTestChange(formData, setFormData)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(AZURE_GLOBAL_FIELD_KEYS.configureOAuth)}
                    errorValue={errors.fieldErrors[AZURE_GLOBAL_FIELD_KEYS.configureOAuth]}
                />
            </ConcreteConfigurationForm>
        </div>
    );
};

AzureBoardForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readonly: PropTypes.bool,
    displayTest: PropTypes.bool,
    displaySave: PropTypes.bool,
    displayDelete: PropTypes.bool
};

AzureBoardForm.defaultProps = {
    readonly: false,
    displayTest: true,
    displaySave: true,
    displayDelete: true
};

export default AzureBoardForm;
