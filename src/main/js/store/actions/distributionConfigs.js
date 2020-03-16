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
    DISTRIBUTION_JOB_UPDATING
} from 'store/actions/types';
import { verifyLoginByStatus } from 'store/actions/session';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';

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
        configurationMessage: message
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
    errors[errorFieldName] = '';
    return {
        type: DISTRIBUTION_JOB_CHECK_DESCRIPTOR_SUCCESS,
        errors
    };
}

function checkingDescriptorGlobalConfigFailure(errorFieldName, response) {
    const errors = {};
    errors[errorFieldName] = response;
    return {
        type: DISTRIBUTION_JOB_CHECK_DESCRIPTOR_FAILURE,
        errors
    };
}

function jobError(type, message, errors) {
    return {
        type,
        configurationMessage: message,
        errors
    };
}

function handleFailureResponse(type, dispatch, response) {
    response.json().then((data) => {
        switch (response.status) {
            case 400:
                return dispatch(jobError(type, data.message, data.errors));
            case 401:
                dispatch(jobError(type, data.message, data.errors));
                return dispatch(verifyLoginByStatus(response.status));
            case 412:
                return dispatch(jobError(type, data.message, data.errors));
            default: {
                return dispatch(jobError(type, data.message, null));
            }
        }
    });
}

export function getDistributionJob(jobId) {
    return (dispatch, getState) => {
        dispatch(fetchingJob());
        const { csrfToken } = getState().session;
        if (jobId) {
            const request = ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, jobId);
            request.then((response) => {
                if (response.ok) {
                    response.json().then((json) => {
                        if (json) {
                            dispatch(jobFetched(json));
                        } else {
                            dispatch(jobFetchError());
                        }
                    });
                } else {
                    switch (response.status) {
                        case 401:
                            dispatch(verifyLoginByStatus(response.status));
                            break;
                        case 403:
                            dispatch(jobFetchErrorMessage('You are not permitted to view this information.'));
                            break;
                        default:
                            dispatch(jobFetchError());
                    }
                }
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
            if (response.ok) {
                response.json().then((json) => {
                    dispatch(saveJobSuccess(json.message));
                });
            } else {
                handleFailureResponse(DISTRIBUTION_JOB_SAVE_ERROR, dispatch, response);
            }
        }).catch(console.error);
    };
}

export function updateDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(updatingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config.jobId, config);
        request.then((response) => {
            if (response.ok) {
                response.json().then((json) => {
                    dispatch(updateJobSuccess(json.message));
                });
            } else {
                handleFailureResponse(DISTRIBUTION_JOB_UPDATE_ERROR, dispatch, response);
            }
        }).catch(console.error);
    };
}

export function testDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(testingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config);
        request.then((response) => {
            if (response.ok) {
                response.json().then((json) => {
                    dispatch(testJobSuccess(json.message));
                });
            } else {
                handleFailureResponse(DISTRIBUTION_JOB_TEST_FAILURE, dispatch, response);
            }
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
