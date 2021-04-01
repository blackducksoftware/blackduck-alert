import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import GlobalTestModal from 'global/GlobalTestModal';
import StatusMessage from 'field/StatusMessage';

const CommonGlobalConfigurationForm = ({
    formData, csrfToken, setFormData, setErrors, displaySave, displayTest, displayDelete, children, testFields
}) => {
    const [showTest, setShowTest] = useState(false);
    const [testFormData, setTestFormData] = useState({});
    const [errorMessage, setErrorMessage] = useState(null);
    const [actionMessage, setActionMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);

    const readRequest = () => ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, formData.descriptorName);
    const testRequest = () => ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, FieldModelUtilities.getFieldModelId(formData));
    const deleteRequest = () => ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, FieldModelUtilities.getFieldModelId(formData));
    const validateRequest = () => ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);

    useEffect(() => {
        const fetchData = async () => {
            const response = await readRequest();
            const data = await response.json();

            const { fieldModels } = data;
            const retrievedModel = (fieldModels && fieldModels.length > 0) ? fieldModels[0] : {};
            setFormData(retrievedModel);
        };

        fetchData();
    }, []);

    const handleTestCancel = () => {
        setShowTest(false);
        setTestFormData({});
    };

    const saveRequest = async (event) => {
        event.preventDefault();
        event.stopPropagation();

        setErrorMessage(null);
        setErrors({});
        const validateResponse = await validateRequest();
        const validateJson = await validateResponse.json();
        if (validateResponse.ok) {
            if (validateJson.hasErrors) {
                setErrorMessage(validateJson.message);
                setErrors(validateJson.errors);
            } else {
                const id = FieldModelUtilities.getFieldModelId(formData);
                const request = (id)
                    ? () => ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id, formData)
                    : () => ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);

                await request();

                const reloadedResponse = await readRequest();
                const reloadedData = await reloadedResponse.json();

                const { fieldModels } = reloadedData;
                const retrievedModel = (fieldModels && fieldModels.length > 0) ? fieldModels[0] : {};

                setFormData(retrievedModel);
                setActionMessage('Save Successful');
            }
        }
    };

    const performDeleteRequest = async () => {
        await deleteRequest();
        setFormData({});
        setActionMessage('Delete Successful');
    };

    return (
        <div>
            <StatusMessage
                id="global-config-status-message"
                errorMessage={errorMessage}
                actionMessage={actionMessage}
                errorIsDetailed={errorIsDetailed}
            />
            <form className="form-horizontal" onSubmit={saveRequest} noValidate>
                <div>
                    {children}
                </div>
                <ConfigButtons
                    includeSave={displaySave}
                    includeTest={displayTest}
                    includeDelete={displayDelete}
                    type="submit"
                    onTestClick={() => setShowTest(true)}
                    onDeleteClick={performDeleteRequest}
                    confirmDeleteMessage="Are you sure you want to delete the configuration?"
                />
                <GlobalTestModal
                    showTestModal={showTest}
                    handleTest={testRequest}
                    handleCancel={handleTestCancel}
                >
                    <div>
                        {testFields}
                    </div>
                </GlobalTestModal>
            </form>
        </div>
    );
};

CommonGlobalConfigurationForm.propTypes = {
    children: PropTypes.node.isRequired,
    formData: PropTypes.object.isRequired,
    csrfToken: PropTypes.string.isRequired,
    setFormData: PropTypes.func.isRequired,
    setErrors: PropTypes.func.isRequired,
    displaySave: PropTypes.bool,
    displayTest: PropTypes.bool,
    displayDelete: PropTypes.bool,
    testFields: PropTypes.node
};

CommonGlobalConfigurationForm.defaultProps = {
    displaySave: true,
    displayTest: true,
    displayDelete: true,
    testFields: null
};

export default CommonGlobalConfigurationForm;
