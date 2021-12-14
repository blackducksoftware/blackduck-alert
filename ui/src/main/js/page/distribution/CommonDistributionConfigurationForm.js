import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'common/button/ConfigButtons';
import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import GlobalTestModal from 'common/global/GlobalTestModal';
import StatusMessage from 'common/StatusMessage';

const CommonDistributionConfigurationForm = ({
    formData,
    setFormData,
    setTestFormData,
    csrfToken,
    setErrors,
    displaySave,
    displayTest,
    displayDelete,
    children,
    testFields,
    buttonIdPrefix,
    afterSuccessfulSave,
    retrieveData,
    createDataToSend,
    createDataToTest,
    errorHandler
}) => {
    const [showTest, setShowTest] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);
    const [actionMessage, setActionMessage] = useState(null);
    const [inProgress, setInProgress] = useState(false);

    const testRequest = (model) => ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, model);
    const deleteRequest = (id) => ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id);
    const validateRequest = (model) => ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, model);

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
        setTestFormData({});
    };

    const performTestRequest = async () => {
        setInProgress(true);
        setActionMessage(null);
        const dataToSend = createDataToTest ? createDataToTest() : formData;
        const response = await testRequest(dataToSend);
        const json = await response.json();
        if (response.ok) {
            const errors = Object.values(json.errors);
            if (json.hasErrors) {
                setErrorIsDetailed(json.detailed);
                if (errors.length !== 0 && errors.every((status) => status.severity === 'WARNING')) {
                    setActionMessage('Test Successful (With Warnings)');
                    setErrors(HttpErrorUtilities.createErrorObject(json));
                } else {
                    setErrorMessage(json.message);
                    setErrors(HttpErrorUtilities.createErrorObject(json));
                }
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
        setInProgress(false);
    };

    const handleTestClick = () => {
        setErrorMessage(null);
        setErrors(HttpErrorUtilities.createEmptyErrorObject());

        if (testFields) {
            setShowTest(true);
        } else {
            performTestRequest();
        }
    };

    // FIXME this seems to throw a memory leak error, investigate
    const performSaveRequest = async (event) => {
        event.preventDefault();
        event.stopPropagation();

        setInProgress(true);
        setActionMessage(null);
        setErrorMessage(null);
        setErrors(HttpErrorUtilities.createEmptyErrorObject());
        const dataToSend = createDataToSend ? createDataToSend() : formData;
        const validateResponse = await validateRequest(dataToSend);
        const validateJson = await validateResponse.json();
        if (validateResponse.ok) {
            if (validateJson.hasErrors) {
                setErrorMessage(validateJson.message);
                setErrors(HttpErrorUtilities.createErrorObject(validateJson));
            } else {
                const id = formData.jobId;
                const request = (id)
                    ? () => ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id, dataToSend)
                    : () => ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, dataToSend);

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
        const response = await deleteRequest(formData.jobId);
        if (response.ok) {
            setFormData({});
            setActionMessage('Delete Successful');
        } else {
            const errorObject = errorHandler.handle(response, await response.json(), false);
            if (errorObject && errorObject.message) {
                setErrorMessage(errorObject.message);
            }
            setActionMessage('Delete Failed');
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
                    includeCancel
                    includeSave={displaySave}
                    includeTest={displayTest}
                    includeDelete={displayDelete}
                    type="submit"
                    onTestClick={handleTestClick}
                    onDeleteClick={performDeleteRequest}
                    onCancelClick={() => afterSuccessfulSave()}
                    confirmDeleteMessage="Are you sure you want to delete the configuration?"
                    performingAction={inProgress}
                />
            </form>
            <GlobalTestModal
                showTestModal={showTest}
                handleTest={performTestRequest}
                handleCancel={handleTestCancel}
                buttonIdPrefix={buttonIdPrefix}
                performingAction={inProgress}
            >
                <div>
                    {testFields}
                </div>
            </GlobalTestModal>
        </div>
    );
};

CommonDistributionConfigurationForm.propTypes = {
    children: PropTypes.node.isRequired,
    formData: PropTypes.object.isRequired,
    csrfToken: PropTypes.string.isRequired,
    setFormData: PropTypes.func.isRequired,
    setErrors: PropTypes.func.isRequired,
    retrieveData: PropTypes.func.isRequired,
    displaySave: PropTypes.bool,
    displayTest: PropTypes.bool,
    displayDelete: PropTypes.bool,
    testFields: PropTypes.node,
    testFormData: PropTypes.object,
    setTestFormData: PropTypes.func,
    buttonIdPrefix: PropTypes.string,
    afterSuccessfulSave: PropTypes.func,
    createDataToSend: PropTypes.func,
    createDataToTest: PropTypes.func,
    errorHandler: PropTypes.object.isRequired
};

CommonDistributionConfigurationForm.defaultProps = {
    displaySave: true,
    displayTest: true,
    displayDelete: false,
    testFields: null,
    testFormData: {},
    setTestFormData: () => null,
    buttonIdPrefix: 'common-form',
    afterSuccessfulSave: () => null,
    createDataToSend: null,
    createDataToTest: null
};

export default CommonDistributionConfigurationForm;
