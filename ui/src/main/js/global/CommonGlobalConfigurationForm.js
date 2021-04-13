import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'js/component/common/ConfigButtons';
import * as ConfigRequestBuilder from 'js/util/configurationRequestBuilder';
import * as FieldModelUtilities from 'js/util/fieldModelUtilities';
import GlobalTestModal from 'js/global/GlobalTestModal';
import StatusMessage from 'js/field/StatusMessage';

const CommonGlobalConfigurationForm = ({
    formData, setFormData, testFormData, setTestFormData, csrfToken, setErrors, displaySave, displayTest, displayDelete, children, testFields
}) => {
    const [showTest, setShowTest] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [actionMessage, setActionMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);

    const readRequest = () => ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, formData.descriptorName);
    const testRequest = (fieldModel) => ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, fieldModel);
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

    const performTestRequest = async () => {
        let copy = JSON.parse(JSON.stringify(formData));
        Object.keys(testFormData).forEach((key) => {
            copy = FieldModelUtilities.updateFieldModelSingleValue(copy, key, testFormData[key]);
        });

        const response = await testRequest(copy);
        if (response.ok) {
            const json = await response.json();
            if (json.hasErrors) {
                setErrorMessage(json.message);
                setErrors(json.errors);
            } else {
                setActionMessage('Test Successful');
            }
        }

        handleTestCancel();
    };

    const handleTestClick = () => {
        setErrorMessage(null);
        setErrors({});

        if (testFields) {
            setShowTest(true);
        } else {
            performTestRequest();
        }
    };

    const performSaveRequest = async (event) => {
        event.preventDefault();
        event.stopPropagation();

        setErrorMessage(null);
        setErrors({});
        const validateResponse = await validateRequest();
        if (validateResponse.ok) {
            const validateJson = await validateResponse.json();
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
            <form className="form-horizontal" onSubmit={performSaveRequest} noValidate>
                <div>
                    {children}
                </div>
                <ConfigButtons
                    includeSave={displaySave}
                    includeTest={displayTest}
                    includeDelete={displayDelete}
                    type="submit"
                    onTestClick={handleTestClick}
                    onDeleteClick={performDeleteRequest}
                    confirmDeleteMessage="Are you sure you want to delete the configuration?"
                />
            </form>
            <GlobalTestModal
                showTestModal={showTest}
                handleTest={performTestRequest}
                handleCancel={handleTestCancel}
            >
                <div>
                    {testFields}
                </div>
            </GlobalTestModal>
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
    testFields: PropTypes.node,
    testFormData: PropTypes.object,
    setTestFormData: PropTypes.func
};

CommonGlobalConfigurationForm.defaultProps = {
    displaySave: true,
    displayTest: true,
    displayDelete: true,
    testFields: null,
    testFormData: {},
    setTestFormData: () => null
};

export default CommonGlobalConfigurationForm;
