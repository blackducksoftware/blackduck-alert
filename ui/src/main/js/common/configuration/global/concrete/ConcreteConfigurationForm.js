import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'common/component/button/ConfigButtons';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import GlobalTestModal from 'common/configuration/global/GlobalTestModal';
import StatusMessage from 'common/component/StatusMessage';

const ConcreteConfigurationForm = ({
    formDataId,
    clearTestForm,
    setErrors,
    getRequest,
    deleteRequest,
    updateRequest,
    createRequest,
    validateRequest,
    testRequest,
    displaySave,
    displayTest,
    displayDelete,
    displayCancel,
    children,
    testFields,
    buttonIdPrefix,
    afterSuccessfulSave,
    readonly,
    errorHandler,
    cancelLabel,
    deleteLabel,
    submitLabel,
    testLabel,
    testButtonClicked,
    postDeleteAction,
    disableTestModalSubmit,
    modalSubmitText,
    testModalButtonTitle,
    ignoreValidation,
    isSaveDisabled,
    isDeleteDisabled
}) => {
    const [showTest, setShowTest] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [actionMessage, setActionMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);
    const [inProgress, setInProgress] = useState(false);
    const [testing, setTesting] = useState(false);

    useEffect(() => {
        getRequest();
    }, []);

    const handleTestCancel = () => {
        setShowTest(false);
        setTesting(false);
        setInProgress(false);
        clearTestForm();
    };

    const performTestRequest = async () => {
        setInProgress(true);
        setTesting(true);
        const response = await testRequest();
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
        testButtonClicked ? testButtonClicked() : null;

        if (testFields) {
            setShowTest(true);
        } else {
            performTestRequest();
        }
    };

    // TODO: Some models do not have IDs and we only allow updates, not creates for some endpoints. Need to find a better way to perform saving without IDs.
    //  The workaround is to make the createRequest and updateRequest the same (Ex. SettingsEncryptionConfiguration).
    const performSaveRequest = async (event) => {
        event.preventDefault();
        event.stopPropagation();

        setInProgress(true);
        setErrorMessage(null);
        setActionMessage(null);
        setErrors(HttpErrorUtilities.createEmptyErrorObject());
        if (ignoreValidation) {
            const request = (formDataId) ? () => updateRequest() : () => createRequest();
            const saveResponse = await request();
            if (saveResponse.ok) {
                await getRequest();
                setActionMessage('Save Successful');
                afterSuccessfulSave();
            } else {
                setErrorMessage('Save Failed');
                const errorObject = errorHandler.handle(saveResponse, await saveResponse.json(), false);
                if (errorObject && errorObject.message) {
                    setErrorMessage(errorObject.message);
                }
            }
        } else {
            const validateResponse = await validateRequest();
            const validateJson = await validateResponse.json();
            if (validateResponse.ok) {
                if (validateJson.hasErrors) {
                    setErrorMessage(validateJson.message);
                    setErrors(HttpErrorUtilities.createErrorObject(validateJson));
                } else {
                    const request = (formDataId) ? () => updateRequest() : () => createRequest();
                    const saveResponse = await request();
                    if (saveResponse.ok) {
                        await getRequest();
                        setActionMessage('Save Successful');
                        afterSuccessfulSave();
                    } else {
                        setErrorMessage('Save Failed');
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
        }
        setInProgress(false);
    };

    const performDeleteRequest = async () => {
        setInProgress(true);
        setActionMessage(null);
        if (formDataId) {
            const response = await deleteRequest(formDataId);
            if (response.ok) {
                await getRequest();
                setActionMessage('Delete Successful');
                postDeleteAction();
            } else {
                const errorObject = errorHandler.handle(response, await response.json(), false);
                if (errorObject && errorObject.message) {
                    setErrorMessage(errorObject.message);
                }
                setErrorMessage('Delete Failed');
            }
        } else {
            await getRequest();
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
                    cancelLabel={cancelLabel}
                    deleteLabel={deleteLabel}
                    submitLabel={submitLabel}
                    testLabel={testLabel}
                    isSaveDisabled={isSaveDisabled}
                    isDeleteDisabled={isDeleteDisabled}
                />
            </form>
            <GlobalTestModal
                showTestModal={showTest}
                handleTest={performTestRequest}
                handleCancel={handleTestCancel}
                buttonIdPrefix={buttonIdPrefix}
                performingAction={testing}
                disableTestModalSubmit={disableTestModalSubmit}
                modalSubmitText={modalSubmitText}
                testModalButtonTitle={testModalButtonTitle}
            >
                <div>
                    {testFields}
                </div>
            </GlobalTestModal>
        </div>
    );
};

ConcreteConfigurationForm.propTypes = {
    children: PropTypes.node.isRequired,
    setErrors: PropTypes.func.isRequired,
    clearTestForm: PropTypes.func,
    getRequest: PropTypes.func.isRequired,
    deleteRequest: PropTypes.func.isRequired,
    updateRequest: PropTypes.func.isRequired,
    createRequest: PropTypes.func,
    validateRequest: PropTypes.func,
    testRequest: PropTypes.func,
    displaySave: PropTypes.bool,
    displayTest: PropTypes.bool,
    displayDelete: PropTypes.bool,
    displayCancel: PropTypes.bool,
    formDataId: PropTypes.string,
    testFields: PropTypes.node,
    buttonIdPrefix: PropTypes.string,
    afterSuccessfulSave: PropTypes.func,
    readonly: PropTypes.bool,
    errorHandler: PropTypes.object.isRequired,
    cancelLabel: PropTypes.string,
    deleteLabel: PropTypes.string,
    submitLabel: PropTypes.string,
    testLabel: PropTypes.string,
    testButtonClicked: PropTypes.func,
    postDeleteAction: PropTypes.func,
    disableTestModalSubmit: PropTypes.bool,
    modalSubmitText: PropTypes.string,
    testModalButtonTitle: PropTypes.string,
    isSaveDisabled: PropTypes.bool,
    isDeleteDisabled: PropTypes.bool
};

ConcreteConfigurationForm.defaultProps = {
    testRequest: () => null,
    displaySave: true,
    displayTest: true,
    displayDelete: true,
    displayCancel: false,
    formDataId: null,
    testFields: null,
    clearTestForm: () => null,
    buttonIdPrefix: 'common-form',
    afterSuccessfulSave: () => null,
    readonly: false,
    postDeleteAction: () => {},
    ignoreValidation: false
};

export default ConcreteConfigurationForm;
