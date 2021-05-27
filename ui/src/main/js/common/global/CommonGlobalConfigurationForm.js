import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'common/button/ConfigButtons';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import GlobalTestModal from 'common/global/GlobalTestModal';
import StatusMessage from 'common/StatusMessage';

const CommonGlobalConfigurationForm = ({
    formData,
    setFormData,
    testFormData,
    setTestFormData,
    csrfToken,
    setErrors,
    displaySave,
    displayTest,
    displayDelete,
    displayCancel,
    children,
    testFields,
    buttonIdPrefix,
    afterSuccessfulSave,
    retrieveData,
    readonly,
    errorHandler
}) => {
    const [showTest, setShowTest] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [actionMessage, setActionMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);
    const [inProgress, setInProgress] = useState(false);
    const [testing, setTesting] = useState(false);

    const testRequest = (fieldModel) => ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, fieldModel);
    const deleteRequest = () => ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, FieldModelUtilities.getFieldModelId(formData));
    const validateRequest = () => ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);

    const fetchData = async () => {
        const content = await retrieveData();
        if (content) {
            setFormData(content);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleTestCancel = () => {
        setShowTest(false);
        setTesting(false);
        setInProgress(false);
        setTestFormData({});
    };

    const performTestRequest = async () => {
        setInProgress(true);
        setTesting(true);
        let copy = JSON.parse(JSON.stringify(formData));
        Object.keys(testFormData).forEach((key) => {
            copy = FieldModelUtilities.updateFieldModelSingleValue(copy, key, testFormData[key]);
        });

        const response = await testRequest(copy);
        const json = await response.json();
        if (response.ok) {
            if (json.hasErrors) {
                setErrorIsDetailed(json.detailed);
                setErrorMessage(json.message);
                setErrors(HttpErrorUtilities.createErrorObject(json));
            } else {
                setActionMessage('Test Successful');
            }
        } else {
            const errorObject = errorHandler.handle(response, json, false);
            if (errorObject && errorObject.message) {
                setErrorMessage(errorObject.message);
            }
        }
        handleTestCancel();
    };

    const handleTestClick = () => {
        setErrorMessage(null);
        setErrors(HttpErrorUtilities.createEmptyErrorObject());
        setActionMessage(null);

        if (testFields) {
            setShowTest(true);
        } else {
            performTestRequest();
        }
    };

    const performSaveRequest = async (event) => {
        event.preventDefault();
        event.stopPropagation();

        setInProgress(true);
        setErrorMessage(null);
        setActionMessage(null);
        setErrors(HttpErrorUtilities.createEmptyErrorObject());
        const validateResponse = await validateRequest();
        const validateJson = await validateResponse.json();
        if (validateResponse.ok) {
            if (validateJson.hasErrors) {
                setErrorMessage(validateJson.message);
                setErrors(HttpErrorUtilities.createErrorObject(validateJson));
            } else {
                const id = FieldModelUtilities.getFieldModelId(formData);
                const request = (id)
                    ? () => ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id, formData)
                    : () => ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);

                const saveResponse = await request();
                if (saveResponse.ok) {
                    await fetchData();
                    setActionMessage('Save Successful');
                    afterSuccessfulSave();
                } else {
                    setActionMessage('Save Failed');
                    const errorObject = errorHandler.handle(saveResponse, await saveResponse.json(), false);
                    if (errorObject && errorObject.message) {
                        setErrorMessage(errorObject.message);
                    }
                }
            }
        } else {
            const errorObject = errorHandler.handle(validateResponse, validateJson, false);
            if (errorObject && errorObject.message) {
                setErrorMessage(errorObject.message);
            }
        }
        setInProgress(false);
    };

    const performDeleteRequest = async () => {
        setInProgress(true);
        setActionMessage(null);
        const id = FieldModelUtilities.getFieldModelId(formData);
        if (id) {
            const response = await deleteRequest(id);
            if (response.ok) {
                const deletedForm = FieldModelUtilities.createEmptyFieldModel([], formData.context, formData.descriptorName);
                setFormData(deletedForm);
                setActionMessage('Delete Successful');
            } else {
                const errorObject = errorHandler.handle(response, await response.json(), false);
                if (errorObject && errorObject.message) {
                    setErrorMessage(errorObject.message);
                }
                setActionMessage('Delete Failed');
            }
        }
        setInProgress(false);
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
                    submitId={`${buttonIdPrefix}-submit`}
                    cancelId={`${buttonIdPrefix}-cancel`}
                    deleteId={`${buttonIdPrefix}-delete`}
                    testId={`${buttonIdPrefix}-test`}
                    includeSave={!readonly && displaySave}
                    includeTest={!readonly && displayTest}
                    includeDelete={!readonly && displayDelete}
                    includeCancel={displayCancel}
                    onCancelClick={afterSuccessfulSave}
                    type="submit"
                    onTestClick={handleTestClick}
                    onDeleteClick={performDeleteRequest}
                    confirmDeleteMessage="Are you sure you want to delete the configuration?"
                    performingAction={inProgress}
                />
            </form>
            <GlobalTestModal
                showTestModal={showTest}
                handleTest={performTestRequest}
                handleCancel={handleTestCancel}
                buttonIdPrefix={buttonIdPrefix}
                performingAction={testing}
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
    retrieveData: PropTypes.func.isRequired,
    displaySave: PropTypes.bool,
    displayTest: PropTypes.bool,
    displayDelete: PropTypes.bool,
    displayCancel: PropTypes.bool,
    testFields: PropTypes.node,
    testFormData: PropTypes.object,
    setTestFormData: PropTypes.func,
    buttonIdPrefix: PropTypes.string,
    afterSuccessfulSave: PropTypes.func,
    readonly: PropTypes.bool,
    errorHandler: PropTypes.object.isRequired
};

CommonGlobalConfigurationForm.defaultProps = {
    displaySave: true,
    displayTest: true,
    displayDelete: true,
    displayCancel: false,
    testFields: null,
    testFormData: {},
    setTestFormData: () => null,
    buttonIdPrefix: 'common-form',
    afterSuccessfulSave: () => null,
    readonly: false
};

export default CommonGlobalConfigurationForm;
