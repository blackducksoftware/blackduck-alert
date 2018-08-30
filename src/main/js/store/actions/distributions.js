import {
    DISTRIBUTION_JOB_FETCHING,
    DISTRIBUTION_JOB_FETCHED,
    DISTRIBUTION_JOB_FETCH_ERROR,
    DISTRIBUTION_JOB_SAVING,
    DISTRIBUTION_JOB_SAVED,
    DISTRIBUTION_JOB_SAVE_ERROR,
    DISTRIBUTION_JOB_UPDATING,
    DISTRIBUTION_JOB_UPDATED,
    DISTRIBUTION_JOB_UPDATE_ERROR,
    DISTRIBUTION_JOB_TESTING,
    DISTRIBUTION_JOB_TEST_SUCCESS,
    DISTRIBUTION_JOB_TEST_FAILURE

} from './types';

import {verifyLoginByStatus} from './session';

function fetchingJob() {
    return {
        type: DISTRIBUTION_JOB_FETCHING
    }
}

function jobFetched(config) {
    return {
        type: DISTRIBUTION_JOB_FETCHED,
        jobs: {
            [config.distributionConfigId]: config
        }
    }
}

function jobFetchError() {
    return {
        type: DISTRIBUTION_JOB_FETCH_ERROR
    }
}

function savingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_SAVING
    }
}

function saveJobSuccess(message) {
    return {
        type: DISTRIBUTION_JOB_SAVED,
        configurationMessage: message
    }
}

function saveJobFailed(errors, message) {
    return {
        type: DISTRIBUTION_JOB_SAVE_ERROR,
        configurationMessage: message,
        errors
    }
}

function updatingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_UPDATING
    }
}

function updateJobSuccess(message) {
    return {
        type: DISTRIBUTION_JOB_UPDATED,
        configurationMessage: message
    }
}

function updateJobFailed(errors, message) {
    return {
        type: DISTRIBUTION_JOB_UPDATE_ERROR,
        configurationMessage: message,
        errors
    }
}

function testingJobConfig() {
    return {
        type: DISTRIBUTION_JOB_TESTING
    }
}

function testJobSuccess(message) {
    return {
        type: DISTRIBUTION_JOB_TEST_SUCCESS,
        configurationMessage: message
    }
}

function testJobFailed(errors, message) {
    return {
        type: DISTRIBUTION_JOB_TEST_FAILURE,
        configurationMessage: message,
        errors
    }
}

export function getDistributionJob(url, id) {
    return (dispatch, getState) => {
        dispatch(fetchingJob());
        if (id) {
            const getUrl = `${url}?id=${id}`;
            fetch(getUrl, {
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json'
                }
            }).then((response) => {
                if(response.ok) {
                    response.json().then((jsonArray) => {
                        if(jsonArray && jsonArray.length > 0) {
                            dispatch(jobFetched(jsonArray[0]));
                        } else {
                            dispatch(jobFetchError());
                        }
                    });
                }
                else {
                    dispatch(verifyLoginByStatus(response.status));
                }
            }).catch(console.error);
        } else {
            dispatch(jobFetchError());
        }
    };
}

export function saveDistributionJob(url, config) {
    return (dispatch, getState) => {
        dispatch(savingJobConfig());
        const {csrfToken} = getState().session;
        fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: config
        }).then((response) => {
            if(response.ok) {
                response.json().then((json) => {
                    const jsonErrors = json.errors;
                    if (jsonErrors) {
                        const errors = {};
                        for (const key in jsonErrors) {
                            if (jsonErrors.hasOwnProperty(key)) {
                                const name = key.concat('Error');
                                const value = jsonErrors[key];
                                errors[name] = value;
                            }
                        }
                        dispatch(saveJobFailed(errors, json.message));
                    } else {
                        dispatch(saveJobSuccess(json.message));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    }
}

export function updateDistributionJob(url, config) {
    return (dispatch, getState) => {
        dispatch(updatingJobConfig());
        const {csrfToken} = getState().session;
        fetch(url, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: config
        }).then((response) => {
            if(response.ok) {
                response.json().then((json) => {
                    const jsonErrors = json.errors;
                    if (jsonErrors) {
                        const errors = {};
                        for (const key in jsonErrors) {
                            if (jsonErrors.hasOwnProperty(key)) {
                                const name = key.concat('Error');
                                const value = jsonErrors[key];
                                errors[name] = value;
                            }
                        }
                        dispatch(updateJobFailed(errors, json.message));
                    } else {
                        dispatch(updateJobSuccess(json.message));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    }
}

export function testDistributionJob(url, config) {
    return (dispatch, getState) => {
        dispatch(testingJobConfig());
        const {csrfToken} = getState().session;
        fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: config
        }).then((response) => {
            if(response.ok) {
                response.json().then((json) => {
                    const jsonErrors = json.errors;
                    if (jsonErrors) {
                        const errors = {};
                        for (const key in jsonErrors) {
                            if (jsonErrors.hasOwnProperty(key)) {
                                const name = key.concat('Error');
                                const value = jsonErrors[key];
                                errors[name] = value;
                            }
                        }
                        dispatch(testJobFailed(errors, json.message));
                    } else {
                        dispatch(testJobSuccess(json.message));
                    }
                });
            } else {
                dispatch(verifyLoginByStatus(response.status));
            }
        }).catch(console.error);
    }
}
