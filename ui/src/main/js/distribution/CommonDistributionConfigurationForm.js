import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import GlobalTestModal from 'global/GlobalTestModal';
import StatusMessage from 'field/StatusMessage';

const CommonDistributionConfigurationForm = ({
    formData,
    setFormData,
    testFormData,
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
    createDataToTest
}) => {
    const [showTest, setShowTest] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [actionMessage, setActionMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);
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
        const dataToSend = createDataToTest ? createDataToTest() : formData;
        const response = await testRequest(dataToSend);
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
        setInProgress(false);
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

        setInProgress(true);
        setErrorMessage(null);
        setErrors({});
        const dataToSend = createDataToSend ? createDataToSend() : formData;
        const validateResponse = await validateRequest(dataToSend);
        if (validateResponse.ok) {
            const validateJson = await validateResponse.json();
            if (validateJson.hasErrors) {
                setErrorMessage(validateJson.message);
                setErrors(validateJson.errors);
            } else {
                const id = formData.jobId;
                const request = (id)
                    ? () => ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id, dataToSend)
                    : () => ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, dataToSend);

                await request();
                await fetchData();

                setActionMessage('Save Successful');
                afterSuccessfulSave();
            }
        }
        setInProgress(false);
    };

    const performDeleteRequest = async () => {
        setInProgress(true);
        await deleteRequest(formData.jobId);
        setFormData({});
        setActionMessage('Delete Successful');
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
    createDataToTest: PropTypes.func
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
