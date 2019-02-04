import {
    DISTRIBUTION_JOB_FETCH_ERROR,
    DISTRIBUTION_JOB_FETCH_ERROR_ALL,
    DISTRIBUTION_JOB_FETCHED,
    DISTRIBUTION_JOB_FETCHED_ALL,
    DISTRIBUTION_JOB_FETCHING,
    DISTRIBUTION_JOB_FETCHING_ALL,
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
import * as ConfigRequestBuilder from '../../util/configurationRequestBuilder';

function fetchingJob() {
    return {
        type: DISTRIBUTION_JOB_FETCHING
    };
}

function jobFetched(config) {
    return {
        type: DISTRIBUTION_JOB_FETCHED,
        jobs: {
            [config.distributionConfigId]: config
        }
    };
}

function jobFetchError() {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR
    };
}

function fetchingAllJobs() {
    return {
        type: DISTRIBUTION_JOB_FETCHING_ALL
    };
}

function allJobsFetched(jobs) {
    return {
        type: DISTRIBUTION_JOB_FETCHED_ALL,
        jobs
    };
}

function fetchingAllJobsError(message) {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR_ALL,
        jobConfigTableMessage: message
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

function jobError(type, message, errors) {
    return {
        type,
        configurationMessage: message,
        errors
    };
}

function handleFailureResponse(type, dispatch, response) {
    response.json()
        .then((data) => {
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

export function getDistributionJob(id) {
    return (dispatch, getState) => {
        dispatch(fetchingJob());
        const { csrfToken } = getState().session;
        ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id);
        if (id) {
            const request = ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id);
            request.then((response) => {
                debugger;
                if (response.ok) {
                    response.json().then((jsonArray) => {
                        if (jsonArray && jsonArray.length > 0) {
                            dispatch(jobFetched(jsonArray[0]));
                        } else {
                            dispatch(jobFetchError());
                        }
                    });
                } else {
                    switch (response.status) {
                        case 401:
                        case 403:
                            dispatch(verifyLoginByStatus(response.status));
                            break;
                        default:
                            dispatch(jobFetchError());
                    }
                }
            }).catch(console.error);
        } else {
            dispatch(jobFetchError());
        }
    };
}

export function saveDistributionJob(config) {
    return (dispatch, getState) => {
        dispatch(savingJobConfig());
        const { csrfToken } = getState().session;
        const request = ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config);
        request.then((response) => {
            debugger;
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
        const request = ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, config.id, config);
        request.then((response) => {
            debugger;
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
            debugger;
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
