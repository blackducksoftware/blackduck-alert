import {
    DISTRIBUTION_JOB_CHECK_DESCRIPTOR,
    DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE,
    DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS,
    DISTRIBUTION_JOB_FETCH_ERROR,
    DISTRIBUTION_JOB_FETCHED,
    DISTRIBUTION_JOB_FETCHING,
    DISTRIBUTION_JOB_SAVE_ERROR,
    DISTRIBUTION_JOB_SAVED,
    DISTRIBUTION_JOB_SAVING,
    DISTRIBUTION_JOB_TEST_FAILURE,
    DISTRIBUTION_JOB_TEST_SUCCESS,
    DISTRIBUTION_JOB_TESTING,
    DISTRIBUTION_JOB_UPDATE_ERROR,
    DISTRIBUTION_JOB_UPDATED,
    DISTRIBUTION_JOB_UPDATING,
    DISTRIBUTION_JOB_VALIDATE_ERROR,
    DISTRIBUTION_JOB_VALIDATED,
    DISTRIBUTION_JOB_VALIDATING
} from 'store/actions/types';
import { unauthorized } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

function fetchingJob() {
    return {
        type: DISTRIBUTION_JOB_FETCHING
    };
}

function jobFetched(config) {
    return {
        type: DISTRIBUTION_JOB_FETCHED,
        job: config
    };
}

function jobFetchError() {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR
    };
}

function jobFetchErrorMessage(message) {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR,
        message
    };
}

function savingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_SAVING
    };
}

function saveJobSuccess(message) {
    return {
        type: DISTRIBUTION_JOB_SAVED,
        configurationMessage: message
    };
}

function updatingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_UPDATING
    };
}

function updateJobSuccess(message) {
    return {
        type: DISTRIBUTION_JOB_UPDATED,
        configurationMessage: message
    };
}

function testingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_TESTING
    };
}

function testJobSuccess(message) {
    return {
        type: DISTRIBUTION_JOB_TEST_SUCCESS,
        configurationMessage: message
    };
}

function checkingDescriptorGlobalConfig() {
    return {
        type: DISTRIBUTION_JOB_CHECK_DESCRIPTOR
    };
}

function checkingDescriptorGlobalConfigSuccess(errorFieldName) {
    const errors = {};
    errors[errorFieldName] = {};
    return {
        type: DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS,
        errors
    };
}

function validatingJob() {
    return {
        type: DISTRIBUTION_JOB_VALIDATING
    };
}

function validatedJob() {
    return {
        type: DISTRIBUTION_JOB_VALIDATED
    };
}

function validateJobError(message, errors) {
    return {
        type: DISTRIBUTION_JOB_VALIDATE_ERROR,
        message,
        errors
    };
}

function checkingDescriptorGlobalConfigFailure(errorFieldName, response) {
    const errors = {};
    // TODO This should be handled on Job validation now that we have warnings we can add a validator method to check if the global config is set.
    errors[errorFieldName] = {
        severity: 'WARNING',
        fieldMessage: response
    };
    return {
        type: DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE,
        errors
    };
}

function jobError(type, message, errors) {
    return {
        type,
        message,
        errors
    };
}

function createErrorHandler(type, defaultHandler) {
    const errorHandlers = [];
    errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
    errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobError(type, HTTPErrorUtils.MESSAGES.FORBIDDEN_ACTION)));
    errorHandlers.push(HTTPErrorUtils.createBadRequestHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createPreconditionFailedHandler(defaultHandler));
    errorHandlers.push(HTTPErrorUtils.createDefaultHandler(defaultHandler));
    return HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
}

export function getDistributionJob(jobId) {
    return (dispatch, getState) => {
        dispatch(fetchingJob());
        const { csrfToken } = getState().session;
        const errorHandlers = [];
        errorHandlers.push(HTTPErrorUtils.createUnauthorizedHandler(unauthorized));
        errorHandlers.push(HTTPErrorUtils.createForbiddenHandler(() => jobFetchErrorMessage(HTTPErrorUtils.MESSAGES.FORBIDDEN_READ)));
        errorHandlers.push(HTTPErrorUtils.createDefaultHandler(jobFetchError));
        if (jobId) {
            const request = ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, jobId);
            request.then((response) => {
                response.json()
                    .then((responseData) => {
                        if (response.ok) {
                            if (responseData) {
                                dispatch(jobFetched(responseData));
                            } else {
                                dispatch(jobFetchError());
                            }
                        } else {
                            const handler = HTTPErrorUtils.createHttpErrorHandler(errorHandlers);
                            dispatch(handler(response.status));
                        }
                    });
            }).catch(console.error);
        } else {
            dispatch(jobFetched({}));
        }
    };
}

export function saveDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(savingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(saveJobSuccess(responseData.message));
                    } else {
                        const defaultHandler = () => jobError(DISTRIBUTION_JOB_SAVE_ERROR, responseData.message(), responseData.errors);
                        const handler = createErrorHandler(DISTRIBUTION_JOB_SAVE_ERROR, defaultHandler());
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function updateDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(updatingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config.jobId, config);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(updateJobSuccess(responseData.message));
                    } else {
                        const defaultHandler = () => jobError(DISTRIBUTION_JOB_UPDATE_ERROR, responseData.message(), responseData.errors);
                        const handler = createErrorHandler(DISTRIBUTION_JOB_UPDATE_ERROR, defaultHandler());
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function testDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(testingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    if (response.ok) {
                        dispatch(testJobSuccess(responseData.message));
                    } else {
                        const defaultHandler = () => jobError(DISTRIBUTION_JOB_TEST_FAILURE, responseData.message(), responseData.errors);
                        const handler = createErrorHandler(DISTRIBUTION_JOB_TEST_FAILURE, defaultHandler());
                        dispatch(handler(response.status));
                    }
                });
        }).catch(console.error);
    };
}

export function checkDescriptorForGlobalConfig(errorFieldName, descriptorName) {
    return (dispatch, getState) => {
        dispatch(checkingDescriptorGlobalConfig());
        const { csrfToken } = getState().session;
        const url = `${ConfigRequestBuilder.JOB_API_URL}/descriptorCheck`;
        const request = fetch(url, {
            credentials: 'same-origin',
            method: 'POST',
            body: descriptorName,
            headers: {
                'content-type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        });
        request.then((response) => {
            if (response.ok) {
                dispatch(checkingDescriptorGlobalConfigSuccess(errorFieldName));
            } else {
                response.json().then((data) => {
                    dispatch(checkingDescriptorGlobalConfigFailure(errorFieldName, data.message));
                });
            }
        }).catch(console.error);
    };
}

export function validateDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(validatingJob());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config);
        request.then((response) => {
            response.json()
                .then((responseData) => {
                    const handler = createErrorHandler(DISTRIBUTION_JOB_VALIDATE_ERROR, () => validateJobError(responseData));
                    if (responseData.errors && !Object.keys(responseData.errors).length) {
                        dispatch(validatedJob());
                    } else if (!response.ok) {
                        dispatch(handler(response.status));
                    } else {
                        dispatch(handler(400));
                    }
                });
        }).catch(console.error);
    };
}
